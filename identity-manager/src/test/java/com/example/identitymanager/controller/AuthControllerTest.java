package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

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
                LocalDateTime.now(),
                null,
                null
        );
    }

    @Test
    void shouldReturnTokenOnSuccessfulLogin() throws Exception {
        // Given
        String loginJson = """
                {
                    "email": "authenticated@example.com",
                    "password": "password123"
                }
                """;

        Authentication auth = new UsernamePasswordAuthenticationToken("authenticated@example.com", "password123");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userService.getUserByEmail("authenticated@example.com")).thenReturn(Optional.of(userDTO));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(3600)))
                .andExpect(jsonPath("$.user.email", is("authenticated@example.com")));
    }

    @Test
    void shouldReturn401OnInvalidCredentials() throws Exception {
        // Given
        String loginJson = """
                {
                    "email": "wrong@example.com",
                    "password": "wrongpassword"
                }
                """;

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @WithMockUser(username = "authenticated@example.com")
    void shouldReturnCurrentUser() throws Exception {
        // Given
        when(userService.getUserByEmail("authenticated@example.com"))
                .thenReturn(Optional.of(userDTO));

        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("authenticated@example.com")))
                .andExpect(jsonPath("$.firstName", is("Auth")));
    }

    @Test
    void shouldReturn404WhenNotAuthenticated() throws Exception {
        // When & Then - Security is disabled in tests, so we get 404 instead of 401
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No authenticated user found"));
    }
}