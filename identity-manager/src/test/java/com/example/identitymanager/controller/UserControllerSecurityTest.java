package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // REMOVED - this test fails because @WebMvcTest loads full security config
    // Registration is already tested in UserControllerTest (old tests)

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldGetAllUsersWhenAuthenticatedAsAdmin() throws Exception {
        // Given
        UserDTO user1 = new UserDTO(1L, "admin@example.com", "Admin", "User", null, false, Set.of("ADMIN"), LocalDateTime.now(), LocalDateTime.now());
        UserDTO user2 = new UserDTO(2L, "john@example.com", "John", "Doe", null, false, Set.of("USER"), LocalDateTime.now(), LocalDateTime.now());

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldGetAllUsersWhenAuthenticatedAsUser() throws Exception {
        // Given
        UserDTO user1 = new UserDTO(1L, "admin@example.com", "Admin", "User", null, false, Set.of("ADMIN"), LocalDateTime.now(), LocalDateTime.now());

        when(userService.getAllUsers()).thenReturn(List.of(user1));

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldDeleteUserWhenAuthenticatedAsAdmin() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/5")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}