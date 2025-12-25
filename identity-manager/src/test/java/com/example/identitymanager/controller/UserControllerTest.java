package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserRegistrationDTO;
import com.example.identitymanager.exception.DuplicateResourceException;
import com.example.identitymanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO userDTO;
    private UserRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        HashSet<String> roles = new HashSet<>();
        roles.add("USER");

        userDTO = new UserDTO(
                1L,
                "test@example.com",
                "Test",
                "User",
                "123456789",
                false,
                roles,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setEmail("newuser@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("New");
        registrationDTO.setLastName("User");
        registrationDTO.setPhone("987654321");
        registrationDTO.setIsPrivacyEnabled(false);
    }

    @Test
    void shouldRegisterNewUser() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")));

        verify(userService).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    void shouldReturnValidationErrorWhenEmailIsInvalid() throws Exception {
        // Given
        registrationDTO.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    void shouldReturnValidationErrorWhenPasswordIsTooShort() throws Exception {
        // Given
        registrationDTO.setPassword("short");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(userService, never()).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new DuplicateResourceException("User", "email", "newuser@example.com"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email: 'newuser@example.com'"));
    }

    @Test
    @WithMockUser
    void shouldGetAllUsers() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Arrays.asList(userDTO));

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("test@example.com")));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser
    void shouldGetUserById() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(userDTO));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser
    void shouldGetUserByEmail() throws Exception {
        // Given
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(userDTO));

        // When & Then
        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService).getUserByEmail("test@example.com");
    }
}