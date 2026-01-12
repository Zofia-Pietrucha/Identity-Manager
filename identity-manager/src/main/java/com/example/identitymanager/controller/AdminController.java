package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserRegistrationDTO;
import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.RoleRepository;
import com.example.identitymanager.repository.UserDao;
import com.example.identitymanager.repository.UserRepository;
import com.example.identitymanager.service.UserService;
import com.example.identitymanager.service.FileStorageService;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.*;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDao userDao;
    private final FileStorageService fileStorageService;  // ADDED for avatar support

    public AdminController(UserService userService,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           UserDao userDao,
                           FileStorageService fileStorageService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDao = userDao;
        this.fileStorageService = fileStorageService;  // ADDED
    }

    // GET /admin/users - List all users with REAL pagination
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> userPage = userService.getAllUsers(pageable);

        // ADDED: Add avatar URL to each user
        userPage.getContent().forEach(user -> {
            if (user.getAvatarFilename() != null) {
                user.setAvatarUrl("/api/users/" + user.getId() + "/avatar");
            }
        });

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
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

    // POST /admin/users - Create new user (WITH AVATAR SUPPORT)
    @PostMapping
    public String createUser(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                             BindingResult result,
                             @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/user-form";
        }

        try {
            userService.registerUser(userDTO);

            // ADDED: Handle avatar upload
            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    // Get the created user to update avatar
                    User user = userRepository.findByEmail(userDTO.getEmail())
                            .orElseThrow(() -> new RuntimeException("User not found after creation"));

                    // Validate file type
                    String contentType = avatarFile.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        throw new IllegalArgumentException("Only image files are allowed");
                    }

                    String filename = fileStorageService.storeFile(avatarFile);
                    user.setAvatarFilename(filename);
                    userRepository.save(user);
                } catch (Exception e) {
                    // Log but don't fail - user is already created
                    System.err.println("Failed to upload avatar: " + e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("success", "User created successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating user: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/user-form";
        }
    }

    // GET /admin/users/edit/{id} - Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        UserDTO user = userService.getUserById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", user);
        model.addAttribute("userId", id);  // ADDED for avatar display
        model.addAttribute("isEdit", true);
        return "admin/user-form";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") UserDTO userDTO,
                             BindingResult bindingResult,
                             @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        // Sprawdź błędy walidacji
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Validation failed. Please check your input.");
            model.addAttribute("isEdit", true);
            model.addAttribute("userId", id);
            return "admin/user-form";
        }

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setPhone(userDTO.getPhone());
            user.setIsPrivacyEnabled(userDTO.getIsPrivacyEnabled());

            // ADDED: Handle avatar upload
            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    // Validate file type
                    String contentType = avatarFile.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        throw new IllegalArgumentException("Only image files are allowed");
                    }

                    // Delete old avatar if exists
                    if (user.getAvatarFilename() != null) {
                        fileStorageService.deleteFile(user.getAvatarFilename());
                    }

                    String filename = fileStorageService.storeFile(avatarFile);
                    user.setAvatarFilename(filename);
                } catch (Exception e) {
                    model.addAttribute("error", "Error uploading avatar: " + e.getMessage());
                    model.addAttribute("isEdit", true);
                    model.addAttribute("userId", id);
                    return "admin/user-form";
                }
            }

            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating user: " + e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("userId", id);
            return "admin/user-form";
        }
    }

    // GET /admin/users/delete/{id} - Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Delete avatar file if exists
                if (user.getAvatarFilename() != null) {
                    fileStorageService.deleteFile(user.getAvatarFilename());
                }

                // Delete user with all related data (delegated to service layer)
                userService.deleteUserWithRelatedData(id);

                redirectAttributes.addFlashAttribute("success", "User deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ADDED: GET /admin/users/{id}/avatar/delete - Delete user avatar
    @GetMapping("/{id}/avatar/delete")
    public String deleteUserAvatar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getAvatarFilename() != null) {
                fileStorageService.deleteFile(user.getAvatarFilename());
                user.setAvatarFilename(null);
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Avatar deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("info", "User has no avatar");
            }

            return "redirect:/admin/users/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting avatar: " + e.getMessage());
            return "redirect:/admin/users/edit/" + id;
        }
    }

    // GET /admin/users/import - Show CSV import form
    @GetMapping("/import")
    public String showImportForm() {
        return "admin/import-csv";
    }

    // POST /admin/users/import - Import users from CSV using JDBC
    @PostMapping("/import")
    public String importUsers(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a CSV file");
            return "redirect:/admin/users/import";
        }

        try {
            List<User> users = parseCsvFile(file);

            Role userRole = roleRepository.findByName(Role.RoleName.USER)
                    .orElseThrow(() -> new RuntimeException("USER role not found"));

            int importedCount = 0;

            // FIXED: Use JdbcTemplate for bulk insert instead of JPA
            for (User user : users) {
                // Insert user via JDBC
                int rowsAffected = userDao.insertUser(user);

                if (rowsAffected > 0 && user.getId() != null) {
                    // Assign USER role via JPA (relation table)
                    Set<Role> roles = new HashSet<>();
                    roles.add(userRole);
                    user.setRoles(roles);
                    userRepository.save(user); // Only to update roles relation
                    importedCount++;
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    "Successfully imported " + importedCount + " users via JdbcTemplate");
            return "redirect:/admin/users";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error importing CSV: " + e.getMessage());
            return "redirect:/admin/users/import";
        }
    }

    // GET /admin/users/export - Export users to CSV using OpenCSV
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"users_export.csv\"");

        List<User> users = userRepository.findAll();

        try (CSVWriter csvWriter = new CSVWriter(response.getWriter())) {
            // CSV Header
            String[] header = {"email", "firstName", "lastName", "phone", "isPrivacyEnabled"};
            csvWriter.writeNext(header);

            // CSV Data
            for (User user : users) {
                String[] data = {
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone() != null ? user.getPhone() : "",
                        String.valueOf(user.getIsPrivacyEnabled())
                };
                csvWriter.writeNext(data);
            }
        }
    }

    // Helper method to parse CSV file using OpenCSV library
    private List<User> parseCsvFile(MultipartFile file) throws Exception {
        List<User> users = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            // Read all rows
            List<String[]> allRows = csvReader.readAll();

            // Skip header row (first row)
            for (int i = 1; i < allRows.size(); i++) {
                String[] fields = allRows.get(i);

                // Validate row has minimum required fields
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