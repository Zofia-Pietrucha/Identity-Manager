package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserRegistrationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(ValidationTestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ValidationTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldReturnSuccessForValidData() throws Exception {
        // Given
        UserRegistrationDTO validDTO = new UserRegistrationDTO();
        validDTO.setEmail("valid@example.com");
        validDTO.setPassword("password123");
        validDTO.setFirstName("John");
        validDTO.setLastName("Doe");

        // When & Then
        mockMvc.perform(post("/api/validation/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("All validation passed!")))
                .andExpect(jsonPath("$.email", is("valid@example.com")));
    }

    @Test
    @WithMockUser
    void shouldReturnValidationErrorForInvalidEmail() throws Exception {
        // Given
        UserRegistrationDTO invalidDTO = new UserRegistrationDTO();
        invalidDTO.setEmail("not-an-email");
        invalidDTO.setPassword("password123");
        invalidDTO.setFirstName("John");
        invalidDTO.setLastName("Doe");

        // When & Then
        mockMvc.perform(post("/api/validation/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @WithMockUser
    void shouldReturnValidationErrorForShortPassword() throws Exception {
        // Given
        UserRegistrationDTO invalidDTO = new UserRegistrationDTO();
        invalidDTO.setEmail("valid@example.com");
        invalidDTO.setPassword("short");
        invalidDTO.setFirstName("John");
        invalidDTO.setLastName("Doe");

        // When & Then
        mockMvc.perform(post("/api/validation/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @WithMockUser
    void shouldReturnValidationErrorForEmptyEmail() throws Exception {
        // Given
        UserRegistrationDTO invalidDTO = new UserRegistrationDTO();
        invalidDTO.setEmail("");
        invalidDTO.setPassword("password123");
        invalidDTO.setFirstName("John");
        invalidDTO.setLastName("Doe");

        // When & Then
        mockMvc.perform(post("/api/validation/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @WithMockUser
    void shouldReturnValidationErrorForMissingFields() throws Exception {
        // Given
        String incompleteJson = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";

        // When & Then
        mockMvc.perform(post("/api/validation/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists());
    }

    @Test
    @WithMockUser
    void shouldReturnMultipleValidationErrors() throws Exception {
        // Given
        UserRegistrationDTO invalidDTO = new UserRegistrationDTO();
        invalidDTO.setEmail("invalid");
        invalidDTO.setPassword("123");
        invalidDTO.setFirstName("");
        invalidDTO.setLastName("");

        // When & Then
        mockMvc.perform(post("/api/validation/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists());
    }
}