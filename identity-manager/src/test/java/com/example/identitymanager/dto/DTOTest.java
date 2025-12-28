package com.example.identitymanager.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DTOTest {

    @Test
    void shouldCreateUserDTO() {
        // Given
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        LocalDateTime now = LocalDateTime.now();

        // When
        UserDTO dto = new UserDTO(1L, "test@test.com", "John", "Doe",
                "123", true, roles, now, now);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("test@test.com");
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getRoles()).contains("USER");
    }

    @Test
    void shouldCreateUserRegistrationDTO() {
        // When
        UserRegistrationDTO dto = new UserRegistrationDTO(
                "test@test.com", "password123", "John", "Doe", "123", false);

        // Then
        assertThat(dto.getEmail()).isEqualTo("test@test.com");
        assertThat(dto.getPassword()).isEqualTo("password123");
    }

    @Test
    void shouldCreateSupportTicketDTO() {
        // When
        SupportTicketDTO dto = new SupportTicketDTO(
                1L, "Subject", "Description", "OPEN",
                1L, "user@test.com", LocalDateTime.now());

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getSubject()).isEqualTo("Subject");
        assertThat(dto.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void shouldCreateCreateTicketRequest() {
        // When
        CreateTicketRequest dto = new CreateTicketRequest(1L, "Subject", "Desc");

        // Then
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getSubject()).isEqualTo("Subject");
    }

    @Test
    void shouldCreateUpdateTicketStatusRequest() {
        // When
        UpdateTicketStatusRequest dto = new UpdateTicketStatusRequest("RESOLVED");

        // Then
        assertThat(dto.getStatus()).isEqualTo("RESOLVED");
    }
}