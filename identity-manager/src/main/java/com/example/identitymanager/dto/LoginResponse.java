package com.example.identitymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String status;
    private String message;
    private UserDTO user;

    public LoginResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}