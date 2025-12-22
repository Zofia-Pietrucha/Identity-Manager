package com.example.identitymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketDTO {

    private Long id;
    private String subject;
    private String description;
    private String status;
    private Long userId;
    private String userEmail;
    private LocalDateTime createdAt;
}