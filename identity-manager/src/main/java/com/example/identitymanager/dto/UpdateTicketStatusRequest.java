package com.example.identitymanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"
}