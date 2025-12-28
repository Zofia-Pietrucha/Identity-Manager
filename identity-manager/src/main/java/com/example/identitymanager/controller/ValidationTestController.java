package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserRegistrationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/validation")
@Tag(name = "Validation Demo", description = "Endpoints to demonstrate Bean Validation")
public class ValidationTestController {

    // POST /api/validation/test - Test validation with detailed error response
    @PostMapping("/test")
    @Operation(summary = "Test validation", description = "Send invalid data to see validation errors")
    public ResponseEntity<Map<String, String>> testValidation(@Valid @RequestBody UserRegistrationDTO dto) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All validation passed!");
        response.put("email", dto.getEmail());
        return ResponseEntity.ok(response);
    }

    // Example validation test cases:
    // 1. Empty email: {"email": "", "password": "password123", "firstName": "John", "lastName": "Doe"}
    // 2. Invalid email: {"email": "notanemail", "password": "password123", "firstName": "John", "lastName": "Doe"}
    // 3. Short password: {"email": "test@example.com", "password": "short", "firstName": "John", "lastName": "Doe"}
    // 4. Missing fields: {"email": "test@example.com", "password": "password123"}
    // 5. Valid data: {"email": "valid@example.com", "password": "password123", "firstName": "John", "lastName": "Doe"}
}