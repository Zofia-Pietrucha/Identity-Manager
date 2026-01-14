package com.example.identitymanager.controller;

import com.example.identitymanager.dto.CreateTicketRequest;
import com.example.identitymanager.dto.CreateTicketRequestUser;
import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.dto.UpdateTicketStatusRequest;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.service.SupportTicketService;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupportTicketController.class)
class SupportTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupportTicketService ticketService;

    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    private SupportTicketDTO ticketDTO;
    private SupportTicketDTO ticketDTO2;

    @BeforeEach
    void setUp() {
        ticketDTO = new SupportTicketDTO(
                1L,
                "Test Issue",
                "Test Description",
                "OPEN",
                1L,
                "user@example.com",
                LocalDateTime.now()
        );

        ticketDTO2 = new SupportTicketDTO(
                2L,
                "Another Issue",
                "Another Description",
                "IN_PROGRESS",
                2L,
                "john@example.com",
                LocalDateTime.now()
        );
    }

    // ==================== GET /api/tickets TESTS ====================

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldGetAllTicketsWhenAdmin() throws Exception {
        // Given
        when(ticketService.getAllTickets()).thenReturn(Arrays.asList(ticketDTO, ticketDTO2));

        // When & Then
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].subject", is("Test Issue")))
                .andExpect(jsonPath("$[1].subject", is("Another Issue")));

        verify(ticketService).getAllTickets();
        verify(ticketService, never()).getTicketsByUserEmail(any());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldGetOnlyOwnTicketsWhenUser() throws Exception {
        // Given
        when(ticketService.getTicketsByUserEmail("user@example.com"))
                .thenReturn(Collections.singletonList(ticketDTO));

        // When & Then
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userEmail", is("user@example.com")));

        verify(ticketService).getTicketsByUserEmail("user@example.com");
        verify(ticketService, never()).getAllTickets();
    }

    // ==================== GET /api/tickets/my TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldGetMyTickets() throws Exception {
        // Given
        when(ticketService.getTicketsByUserEmail("user@example.com"))
                .thenReturn(Collections.singletonList(ticketDTO));

        // When & Then
        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].subject", is("Test Issue")));

        verify(ticketService).getTicketsByUserEmail("user@example.com");
    }

    // ==================== GET /api/tickets/{id} TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldGetOwnTicketById() throws Exception {
        // Given
        when(ticketService.getTicketById(1L)).thenReturn(ticketDTO);

        // When & Then
        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is("Test Issue")));

        verify(ticketService).getTicketById(1L);
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = {"USER"})
    void shouldReturn403WhenUserAccessesOtherUserTicket() throws Exception {
        // Given
        when(ticketService.getTicketById(1L)).thenReturn(ticketDTO);

        // When & Then
        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldGetAnyTicketByIdWhenAdmin() throws Exception {
        // Given
        when(ticketService.getTicketById(1L)).thenReturn(ticketDTO);

        // When & Then
        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is("Test Issue")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldReturn404WhenTicketNotFound() throws Exception {
        // Given
        when(ticketService.getTicketById(999L))
                .thenThrow(new ResourceNotFoundException("Ticket", "id", 999L));

        // When & Then
        mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /api/tickets/user/{userId} TESTS ====================

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldGetTicketsByUserIdWhenAdmin() throws Exception {
        // Given
        when(ticketService.getTicketsByUserId(1L))
                .thenReturn(Collections.singletonList(ticketDTO));

        // When & Then
        mockMvc.perform(get("/api/tickets/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(ticketService).getTicketsByUserId(1L);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldReturn403WhenUserAccessesTicketsByUserId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tickets/user/1"))
                .andExpect(status().isForbidden());

        verify(ticketService, never()).getTicketsByUserId(any());
    }

    // ==================== POST /api/tickets TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldCreateTicketForCurrentUser() throws Exception {
        // Given
        CreateTicketRequestUser request = new CreateTicketRequestUser("New Issue", "Description");
        when(ticketService.createTicketForCurrentUser(eq("user@example.com"), eq("New Issue"), eq("Description")))
                .thenReturn(ticketDTO);

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject", is("Test Issue")));

        verify(ticketService).createTicketForCurrentUser("user@example.com", "New Issue", "Description");
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldReturnValidationErrorWhenSubjectIsBlank() throws Exception {
        // Given
        CreateTicketRequestUser request = new CreateTicketRequestUser("", "Description");

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).createTicketForCurrentUser(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldReturnValidationErrorWhenDescriptionIsBlank() throws Exception {
        // Given
        CreateTicketRequestUser request = new CreateTicketRequestUser("Subject", "");

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).createTicketForCurrentUser(any(), any(), any());
    }

    // ==================== POST /api/tickets/admin TESTS ====================

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldCreateTicketForAnyUserWhenAdmin() throws Exception {
        // Given
        CreateTicketRequest request = new CreateTicketRequest(2L, "Admin Created", "Description");
        when(ticketService.createTicket(eq(2L), eq("Admin Created"), eq("Description")))
                .thenReturn(ticketDTO2);

        // When & Then
        mockMvc.perform(post("/api/tickets/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(ticketService).createTicket(2L, "Admin Created", "Description");
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldReturn403WhenUserTriesToUseAdminEndpoint() throws Exception {
        // Given
        CreateTicketRequest request = new CreateTicketRequest(2L, "Subject", "Description");

        // When & Then
        mockMvc.perform(post("/api/tickets/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(ticketService, never()).createTicket(any(), any(), any());
    }

    // ==================== PATCH /api/tickets/{id}/status TESTS ====================

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldUpdateTicketStatusWhenAdmin() throws Exception {
        // Given
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest("RESOLVED");
        SupportTicketDTO updatedTicket = new SupportTicketDTO(
                1L, "Test Issue", "Description", "RESOLVED",
                1L, "user@example.com", LocalDateTime.now()
        );
        when(ticketService.updateTicketStatus(eq(1L), eq(SupportTicket.TicketStatus.RESOLVED)))
                .thenReturn(updatedTicket);

        // When & Then
        mockMvc.perform(patch("/api/tickets/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));

        verify(ticketService).updateTicketStatus(1L, SupportTicket.TicketStatus.RESOLVED);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldReturn403WhenUserTriesToUpdateStatus() throws Exception {
        // Given
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest("RESOLVED");

        // When & Then
        mockMvc.perform(patch("/api/tickets/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(ticketService, never()).updateTicketStatus(any(), any());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldReturnBadRequestForInvalidStatus() throws Exception {
        // Given
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest("INVALID_STATUS");

        // When & Then
        mockMvc.perform(patch("/api/tickets/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}