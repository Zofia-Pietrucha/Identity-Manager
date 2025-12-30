package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;
import com.example.identitymanager.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserDashboardController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserDashboardController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        UserDTO user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate avatar URL if user has avatar
        if (user.getAvatarFilename() != null) {
            user.setAvatarUrl("/api/users/" + user.getId() + "/avatar");
        }

        model.addAttribute("user", user);
        return "user/dashboard";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication authentication,
                                UserUpdateDTO updateDTO,
                                @RequestParam(value = "isPrivacyEnabled", defaultValue = "false") Boolean isPrivacyEnabled,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                RedirectAttributes redirectAttributes) {
        String email = authentication.getName();

        // Get current user
        UserDTO currentUser = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Handle avatar upload if provided
        if (avatar != null && !avatar.isEmpty()) {
            try {
                // Delete old avatar if exists
                if (currentUser.getAvatarFilename() != null) {
                    fileStorageService.deleteFile(currentUser.getAvatarFilename());
                }

                // Save new avatar
                String filename = fileStorageService.storeFile(avatar);

                // Update user with new avatar filename
                userService.updateUserAvatar(email, filename);

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload avatar: " + e.getMessage());
                return "redirect:/user/dashboard";
            }
        }

        // Update user profile (firstName, lastName, phone)
        userService.updateUserProfile(email, updateDTO);

        // Update privacy setting separately
        userService.updatePrivacySettings(email, isPrivacyEnabled);

        // Add success message
        redirectAttributes.addAttribute("success", "true");

        return "redirect:/user/dashboard";
    }

    @PostMapping("/avatar/delete")
    public String deleteAvatar(Authentication authentication, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();

        // Get current user
        UserDTO currentUser = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete avatar file if exists
        if (currentUser.getAvatarFilename() != null) {
            try {
                fileStorageService.deleteFile(currentUser.getAvatarFilename());
                userService.updateUserAvatar(email, null);

                redirectAttributes.addAttribute("success", "true");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to delete avatar: " + e.getMessage());
            }
        }

        return "redirect:/user/dashboard";
    }
}