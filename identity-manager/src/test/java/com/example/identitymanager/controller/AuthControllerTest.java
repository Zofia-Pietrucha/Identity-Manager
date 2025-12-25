package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        HashSet<String> roles = new HashSet<>();
        roles.add("USER");

        userDTO = new UserDTO(
                1L,
                "authenticated@example.com",
                "Auth",
                "User",
                "123456789",
                false,
                roles,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(username = "authenticated@example.com")
    void shouldReturnCurrentUser() throws Exception {
        // Given
        when(userService.getUserByEmail("authenticated@example.com"))
                .thenReturn(Optional.of(userDTO));

        // When & Then
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("authenticated@example.com")))
                .andExpect(jsonPath("$.firstName", is("Auth")));
    }

    @Test
    void shouldReturn404WhenNotAuthenticated() throws Exception {
        // When & Then - Security is disabled in tests, so we get 404 instead of 401
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No authenticated user found"));
    }
}