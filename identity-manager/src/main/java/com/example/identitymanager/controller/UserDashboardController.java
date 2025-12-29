package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserDashboardController {

    private final UserService userService;

    public UserDashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        UserDTO user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "user/dashboard";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication authentication,
                                UserUpdateDTO updateDTO,
                                @RequestParam(value = "isPrivacyEnabled", defaultValue = "false") Boolean isPrivacyEnabled,
                                RedirectAttributes redirectAttributes) {
        String email = authentication.getName();

        // Update user profile (firstName, lastName, phone)
        userService.updateUserProfile(email, updateDTO);

        // Update privacy setting separately
        userService.updatePrivacySettings(email, isPrivacyEnabled);

        // Add success message
        redirectAttributes.addAttribute("success", "true");

        return "redirect:/user/dashboard";
    }
}