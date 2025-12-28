package com.example.identitymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String status;           // "success" or "error"
    private String message;           // Optional message
    private String accessToken;       // JWT-like token
    private String tokenType;         // "Bearer"
    private Long expiresIn;          // Token expiration in seconds (3600 = 1 hour)
    private UserDTO user;            // User data

    // Constructor for error responses (without token)
    public LoginResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}