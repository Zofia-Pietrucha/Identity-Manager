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

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminController(UserService userService,
                           UserRepository userRepository,
                           RoleRepository roleRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
}