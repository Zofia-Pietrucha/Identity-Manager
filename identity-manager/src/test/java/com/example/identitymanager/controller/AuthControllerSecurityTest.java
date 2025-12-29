package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.example.identitymanager.config.SecurityConfig.class)
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldGetCurrentUserWhenAuthenticated() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO(
                2L,
                "john@example.com",
                "John",
                "Doe",
                null,
                false,
                Set.of("USER"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );

        when(userService.getUserByEmail("john@example.com")).thenReturn(Optional.of(userDTO));

        // When & Then - FIXED: zmieniono ścieżkę z /api/me na /api/auth/me
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenNotAuthenticatedForGetMe() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldUpdateCurrentUserProfileWhenAuthenticated() throws Exception {
        // Given
        UserDTO updatedUser = new UserDTO(
                2L,
                "john@example.com",
                "Jonathan",
                "Smith",
                "+48123456789",
                false,
                Set.of("USER"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );

        when(userService.updateUserProfile(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(updatedUser);

        String requestBody = """
                {
                    "firstName": "Jonathan",
                    "lastName": "Smith",
                    "phone": "+48123456789"
                }
                """;

        // When & Then - FIXED: zmieniono ścieżkę z /api/me na /api/auth/me
        mockMvc.perform(put("/api/auth/me")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jonathan"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenNotAuthenticatedForUpdateMe() throws Exception {
        // Given
        String requestBody = """
                {
                    "firstName": "Jonathan",
                    "lastName": "Smith",
                    "phone": "+48123456789"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/auth/me")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldUpdatePrivacySettingsWhenAuthenticated() throws Exception {
        // Given
        UserDTO updatedUser = new UserDTO(
                2L,
                "john@example.com",
                "John",
                "Doe",
                null,
                true, // privacy enabled
                Set.of("USER"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );

        when(userService.updatePrivacySettings(anyString(), org.mockito.ArgumentMatchers.anyBoolean()))
                .thenReturn(updatedUser);

        String requestBody = """
                {
                    "isPrivacyEnabled": true
                }
                """;

        // When & Then
        mockMvc.perform(patch("/api/auth/me/privacy")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPrivacyEnabled").value(true));
    }
}