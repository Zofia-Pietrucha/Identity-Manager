package com.example.identitymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isPrivacyEnabled;
    private Set<String> roles;  // Just role names, not full Role objects
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ADDED: Avatar fields
    private String avatarFilename;  // Stored filename in database
    private String avatarUrl;       // URL to display avatar (generated, not stored)
}