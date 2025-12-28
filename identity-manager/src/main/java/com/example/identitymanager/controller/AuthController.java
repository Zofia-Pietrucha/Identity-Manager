package com.example.identitymanager.controller;

import com.example.identitymanager.dto.LoginRequest;
import com.example.identitymanager.dto.LoginResponse;
import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // POST /api/auth/login - Login endpoint returning token
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns access token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Get user data
            UserDTO user = userService.getUserByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginRequest.getEmail()));

            // Generate token (simple Base64 token for demonstration)
            // In production, use JWT with proper signing
            String tokenData = loginRequest.getEmail() + ":" + System.currentTimeMillis();
            String accessToken = Base64.getEncoder().encodeToString(tokenData.getBytes(StandardCharsets.UTF_8));

            // Create success response with token
            LoginResponse response = new LoginResponse(
                    "success",
                    "Login successful",
                    accessToken,
                    "Bearer",
                    3600L,  // Token expires in 1 hour
                    user
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            LoginResponse response = new LoginResponse(
                    "error",
                    "Invalid email or password"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // GET /api/me - Get current logged-in user
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns information about the currently authenticated user")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        String email = authentication.getName();
        UserDTO user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return ResponseEntity.ok(user);
    }

    // PUT /api/me - Update current user profile
    @PutMapping("/me")
    @Operation(summary = "Update current user", description = "Updates profile information of the currently authenticated user")
    public ResponseEntity<UserDTO> updateCurrentUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        String email = authentication.getName();
        UserDTO updatedUser = userService.updateUserProfile(email, updateDTO);

        return ResponseEntity.ok(updatedUser);
    }

    // PATCH /api/me/privacy - Update privacy settings
    @PatchMapping("/me/privacy")
    @Operation(summary = "Update privacy settings", description = "Toggles privacy settings for the current user")
    public ResponseEntity<UserDTO> updatePrivacySettings(@RequestBody Map<String, Boolean> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        String email = authentication.getName();
        Boolean isPrivacyEnabled = request.get("isPrivacyEnabled");

        if (isPrivacyEnabled == null) {
            throw new IllegalArgumentException("isPrivacyEnabled field is required");
        }

        UserDTO updatedUser = userService.updatePrivacySettings(email, isPrivacyEnabled);

        return ResponseEntity.ok(updatedUser);
    }
}