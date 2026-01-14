package com.example.identitymanager.controller;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.service.FileStorageService;
import com.example.identitymanager.service.SupportTicketService;
import com.example.identitymanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserDashboardController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final SupportTicketService ticketService;

    public UserDashboardController(UserService userService,
                                   FileStorageService fileStorageService,
                                   SupportTicketService ticketService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.ticketService = ticketService;
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

        // Pobierz tickety użytkownika
        List<SupportTicketDTO> tickets = ticketService.getTicketsByUserEmail(email);

        model.addAttribute("user", user);
        model.addAttribute("tickets", tickets);
        return "user/dashboard";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication authentication,
                                @Valid UserUpdateDTO updateDTO,
                                BindingResult bindingResult,
                                @RequestParam(value = "isPrivacyEnabled", defaultValue = "false") Boolean isPrivacyEnabled,
                                @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                RedirectAttributes redirectAttributes) {

        // Sprawdź błędy walidacji
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            bindingResult.getFieldErrors().forEach(error ->
                    errorMessage.append(error.getField())
                            .append(" - ")
                            .append(error.getDefaultMessage())
                            .append("; ")
            );
            redirectAttributes.addFlashAttribute("error", errorMessage.toString());
            return "redirect:/user/dashboard";
        }

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

    // POST /user/tickets - Create new ticket
    @PostMapping("/tickets")
    public String createTicket(Authentication authentication,
                               @RequestParam("subject") String subject,
                               @RequestParam("description") String description,
                               RedirectAttributes redirectAttributes) {

        String email = authentication.getName();

        // Walidacja
        if (subject == null || subject.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("ticketError", "Subject is required");
            return "redirect:/user/dashboard";
        }
        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("ticketError", "Description is required");
            return "redirect:/user/dashboard";
        }

        try {
            ticketService.createTicketForCurrentUser(email, subject.trim(), description.trim());
            redirectAttributes.addFlashAttribute("ticketSuccess", "Ticket created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("ticketError", "Failed to create ticket: " + e.getMessage());
        }

        return "redirect:/user/dashboard";
    }
}