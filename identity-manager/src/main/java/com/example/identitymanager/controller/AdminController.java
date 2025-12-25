package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserRegistrationDTO;
import com.example.identitymanager.exception.DuplicateResourceException;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.RoleRepository;
import com.example.identitymanager.repository.UserRepository;
import com.example.identitymanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserService userService,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET /admin/users - List all users
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        List<UserDTO> allUsers = userService.getAllUsers();

        int totalUsers = allUsers.size();
        int totalPages = (int) Math.ceil((double) totalUsers / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalUsers);

        List<UserDTO> pageUsers = allUsers.subList(startIndex, endIndex);

        model.addAttribute("users", pageUsers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);

        return "admin/users-list";
    }

    // GET /admin/users/new - Show create form
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    // POST /admin/users - Create new user
    @PostMapping
    public String createUser(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/user-form";
        }

        try {
            userService.registerUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "User created successfully!");
            return "redirect:/admin/users";
        } catch (DuplicateResourceException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/user-form";
        }
    }

    // GET /admin/users/{id}/edit - Show edit form
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

            model.addAttribute("user", user);
            model.addAttribute("userId", id);
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    // POST /admin/users/{id}/edit - Update user
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") User userUpdate,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

            // Update fields
            existingUser.setFirstName(userUpdate.getFirstName());
            existingUser.setLastName(userUpdate.getLastName());
            existingUser.setPhone(userUpdate.getPhone());
            existingUser.setIsPrivacyEnabled(userUpdate.getIsPrivacyEnabled());

            // Don't update email or password here for security
            userRepository.save(existingUser);

            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    // GET /admin/users/{id}/delete - Delete user
    @GetMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }


    // GET /admin/users/import - Show import form
    @GetMapping("/import")
    public String showImportForm() {
        return "admin/import-csv";
    }

    // POST /admin/users/import - Import users from CSV
    @PostMapping("/import")
    public String importUsers(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a CSV file to upload");
            return "redirect:/admin/users/import";
        }

        try {
            List<User> importedUsers = parseCsvFile(file);
            int successCount = 0;
            int errorCount = 0;

            for (User user : importedUsers) {
                try {
                    // Check if email already exists
                    if (!userRepository.existsByEmail(user.getEmail())) {
                        // Assign default USER role
                        Role userRole = roleRepository.findByName(Role.RoleName.USER)
                                .orElseThrow(() -> new RuntimeException("USER role not found"));
                        user.getRoles().add(userRole);

                        userRepository.save(user);
                        successCount++;
                    } else {
                        errorCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    String.format("Import completed: %d users added, %d skipped/errors", successCount, errorCount));
            return "redirect:/admin/users";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error parsing CSV file: " + e.getMessage());
            return "redirect:/admin/users/import";
        }
    }

    // GET /admin/users/export - Export users to CSV
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"users_export.csv\"");

        List<User> users = userRepository.findAll();

        try (PrintWriter writer = response.getWriter()) {
            // CSV Header
            writer.println("email,firstName,lastName,phone,isPrivacyEnabled");

            // CSV Data
            for (User user : users) {
                writer.printf("%s,%s,%s,%s,%s%n",
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone() != null ? user.getPhone() : "",
                        user.getIsPrivacyEnabled()
                );
            }
        }
    }

    // Helper method to parse CSV file
    private List<User> parseCsvFile(MultipartFile file) throws Exception {
        List<User> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                String[] fields = line.split(",");
                if (fields.length < 5) {
                    continue; // Skip invalid lines
                }

                User user = new User();
                user.setEmail(fields[0].trim());
                user.setFirstName(fields[1].trim());
                user.setLastName(fields[2].trim());
                user.setPhone(fields[3].trim().isEmpty() ? null : fields[3].trim());
                user.setIsPrivacyEnabled(Boolean.parseBoolean(fields[4].trim()));
                user.setPassword(passwordEncoder.encode("password123")); // Hash default password

                users.add(user);
            }
        }

        return users;
    }
}