package com.example.identitymanager.controller;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.service.SupportTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupportTicketController.class)
class SupportTicketControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupportTicketService ticketService;

    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    private SupportTicketDTO testTicketDTO;

    @BeforeEach
    void setUp() {
        testTicketDTO = new SupportTicketDTO(
                1L,
                "Test Issue",
                "Test Description",
                "OPEN",
                2L,
                "john@example.com",
                LocalDateTime.now()
        );
    }

    // ==================== GET /api/tickets TESTS ====================

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldGetTicketsWhenAuthenticatedAsUser() throws Exception {
        // Given - USER gets only their own tickets
        when(ticketService.getTicketsByUserEmail("john@example.com"))
                .thenReturn(Collections.singletonList(testTicketDTO));

        // When & Then
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("Test Issue"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldGetAllTicketsWhenAuthenticatedAsAdmin() throws Exception {
        // Given - ADMIN gets all tickets
        when(ticketService.getAllTickets())
                .thenReturn(Collections.singletonList(testTicketDTO));

        // When & Then
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("Test Issue"));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== POST /api/tickets TESTS ====================

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldCreateTicketWhenAuthenticated() throws Exception {
        // Given - User creates ticket for themselves (no userId in request)
        when(ticketService.createTicketForCurrentUser(eq("john@example.com"), anyString(), anyString()))
                .thenReturn(testTicketDTO);

        String requestBody = """
                {
                    "subject": "New Issue",
                    "description": "Description"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Test Issue"));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenNotAuthenticatedForCreateTicket() throws Exception {
        // Given
        String requestBody = """
                {
                    "subject": "New Issue",
                    "description": "Description"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    // ==================== PATCH /api/tickets/{id}/status TESTS ====================

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldUpdateTicketStatusWhenAuthenticatedAsAdmin() throws Exception {
        // Given
        SupportTicketDTO updatedTicket = new SupportTicketDTO(
                1L,
                "Test",
                "Description",
                "RESOLVED",
                2L,
                "john@example.com",
                LocalDateTime.now()
        );

        when(ticketService.updateTicketStatus(anyLong(), any(SupportTicket.TicketStatus.class)))
                .thenReturn(updatedTicket);

        String requestBody = """
                {
                    "status": "RESOLVED"
                }
                """;

        // When & Then
        mockMvc.perform(patch("/api/tickets/1/status")
                        .with(csrf())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldReturn403WhenUserTriesToUpdateStatus() throws Exception {
        // Given
        String requestBody = """
                {
                    "status": "RESOLVED"
                }
                """;

        // When & Then
        mockMvc.perform(patch("/api/tickets/1/status")
                        .with(csrf())
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }
}