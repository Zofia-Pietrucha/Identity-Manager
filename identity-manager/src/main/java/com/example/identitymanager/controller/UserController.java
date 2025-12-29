package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserRegistrationDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.UserRepository;
import com.example.identitymanager.service.FileStorageService;
import com.example.identitymanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public UserController(UserService userService,
                          FileStorageService fileStorageService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    // POST /api/users - Register new user
    @PostMapping
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        UserDTO createdUser = userService.registerUser(registrationDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // GET /api/users - Get all users (simple list - backward compatibility)
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // GET /api/users/paginated - Get users with pagination
    @GetMapping("/paginated")
    @Operation(summary = "Get users with pagination", description = "Retrieves users with pagination support")
    public ResponseEntity<Map<String, Object>> getUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<UserDTO> userPage = userService.getAllUsers(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("users", userPage.getContent());
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // GET /api/users/search - Search users with pagination
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by keyword with pagination")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> userPage = userService.searchUsers(keyword, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("users", userPage.getContent());
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // GET /api/users/by-role/{roleName} - Get users by role (using custom @Query)
    @GetMapping("/by-role/{roleName}")
    @Operation(summary = "Get users by role", description = "Retrieves users with specific role")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String roleName) {
        List<UserDTO> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    // GET /api/users/stats/privacy - Get privacy statistics (using custom @Query)
    @GetMapping("/stats/privacy")
    @Operation(summary = "Get privacy statistics", description = "Returns count of users with privacy enabled")
    public ResponseEntity<Map<String, Long>> getPrivacyStats() {
        long count = userService.countUsersWithPrivacyEnabled();
        Map<String, Long> stats = new HashMap<>();
        stats.put("usersWithPrivacyEnabled", count);
        return ResponseEntity.ok(stats);
    }

    // GET /api/users/{id} - Get user by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return ResponseEntity.ok(user);
    }

    // GET /api/users/email/{email} - Get user by email
    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieves a specific user by their email")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return ResponseEntity.ok(user);
    }

    // PUT /api/users/{id} - Update user
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user's information")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        UserDTO updatedUser = userService.updateUser(id, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // DELETE /api/users/{id} - Delete user
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== AVATAR ENDPOINTS ====================

    // POST /api/users/{id}/avatar - Upload avatar
    @PostMapping("/{id}/avatar")
    @Operation(summary = "Upload user avatar", description = "Upload an avatar image for a user")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Get user
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Delete old avatar if exists
        if (user.getAvatarFilename() != null) {
            fileStorageService.deleteFile(user.getAvatarFilename());
        }

        // Store new file using Files.copy()
        String filename = fileStorageService.storeFile(file);
        user.setAvatarFilename(filename);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Avatar uploaded successfully");
        response.put("filename", filename);

        return ResponseEntity.ok(response);
    }

    // GET /api/users/{id}/avatar - Download avatar
    @GetMapping("/{id}/avatar")
    @Operation(summary = "Download user avatar", description = "Download user's avatar image")
    public ResponseEntity<Resource> downloadAvatar(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.getAvatarFilename() == null) {
            throw new ResourceNotFoundException("Avatar not found for user with id: " + id);
        }

        // Load file as Resource (ResponseEntity<Resource> = ResponseEntity<byte[]> equivalent)
        Resource resource = fileStorageService.loadFileAsResource(user.getAvatarFilename());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + user.getAvatarFilename() + "\"")
                .body(resource);
    }

    // DELETE /api/users/{id}/avatar - Delete avatar
    @DeleteMapping("/{id}/avatar")
    @Operation(summary = "Delete user avatar", description = "Delete user's avatar image")
    public ResponseEntity<Map<String, String>> deleteAvatar(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.getAvatarFilename() == null) {
            throw new ResourceNotFoundException("Avatar not found for user with id: " + id);
        }

        fileStorageService.deleteFile(user.getAvatarFilename());
        user.setAvatarFilename(null);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Avatar deleted successfully");

        return ResponseEntity.ok(response);
    }
}