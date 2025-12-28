package com.example.identitymanager.controller;

import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.UserDao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/jdbc")
@Tag(name = "JDBC Operations", description = "Direct JDBC database operations for testing")
public class JdbcTestController {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public JdbcTestController(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    // GET /api/jdbc/users - Find all users via JDBC
    @GetMapping("/users")
    @Operation(summary = "Get all users via JDBC", description = "Retrieves all users using JdbcTemplate")
    public ResponseEntity<List<User>> getAllUsersJdbc() {
        List<User> users = userDao.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // GET /api/jdbc/users/{id} - Find user by ID via JDBC
    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID via JDBC", description = "Retrieves a user using JdbcTemplate")
    public ResponseEntity<User> getUserByIdJdbc(@PathVariable Long id) {
        Optional<User> user = userDao.findUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/jdbc/users - Insert user via JDBC
    @PostMapping("/users")
    @Operation(summary = "Insert user via JDBC", description = "Creates a new user using JdbcTemplate update()")
    public ResponseEntity<Map<String, Object>> insertUserJdbc(@RequestBody Map<String, String> request) {
        User user = new User();
        user.setEmail(request.get("email"));
        user.setPassword(passwordEncoder.encode(request.get("password")));
        user.setFirstName(request.get("firstName"));
        user.setLastName(request.get("lastName"));
        user.setPhone(request.get("phone"));
        user.setIsPrivacyEnabled(Boolean.parseBoolean(request.getOrDefault("isPrivacyEnabled", "false")));

        int rowsAffected = userDao.insertUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("rowsAffected", rowsAffected);
        response.put("userId", user.getId());
        response.put("message", "User inserted via JdbcTemplate");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // PUT /api/jdbc/users/{id} - Update user via JDBC
    @PutMapping("/users/{id}")
    @Operation(summary = "Update user via JDBC", description = "Updates a user using JdbcTemplate update()")
    public ResponseEntity<Map<String, Object>> updateUserJdbc(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        Optional<User> existingUser = userDao.findUserById(id);
        if (existingUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = existingUser.get();
        user.setFirstName(request.get("firstName"));
        user.setLastName(request.get("lastName"));
        user.setPhone(request.get("phone"));
        if (request.containsKey("isPrivacyEnabled")) {
            user.setIsPrivacyEnabled(Boolean.parseBoolean(request.get("isPrivacyEnabled")));
        }

        int rowsAffected = userDao.updateUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("rowsAffected", rowsAffected);
        response.put("message", "User updated via JdbcTemplate");

        return ResponseEntity.ok(response);
    }

    // DELETE /api/jdbc/users/{id} - Delete user via JDBC
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user via JDBC", description = "Deletes a user using JdbcTemplate update()")
    public ResponseEntity<Map<String, Object>> deleteUserJdbc(@PathVariable Long id) {
        int rowsAffected = userDao.deleteUserById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("rowsAffected", rowsAffected);
        response.put("message", rowsAffected > 0 ? "User deleted via JdbcTemplate" : "User not found");

        return rowsAffected > 0 ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // GET /api/jdbc/stats - Statistics via JDBC
    @GetMapping("/stats")
    @Operation(summary = "Get user statistics via JDBC", description = "Returns user count using JdbcTemplate")
    public ResponseEntity<Map<String, Object>> getUserStatsJdbc() {
        int totalUsers = userDao.countUsers();
        List<User> privacyEnabledUsers = userDao.findUsersByPrivacyEnabled(true);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("usersWithPrivacyEnabled", privacyEnabledUsers.size());

        return ResponseEntity.ok(stats);
    }
}