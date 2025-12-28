package com.example.identitymanager.controller;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.service.SupportTicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupportTicketController.class)
@Import(com.example.identitymanager.config.SecurityConfig.class)
class SupportTicketControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupportTicketService ticketService;

    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldGetAllTicketsWhenAuthenticated() throws Exception {
        // Given - FIXED: 7 parameters (id, subject, description, status, userId, userEmail, createdAt)
        SupportTicketDTO ticket = new SupportTicketDTO(
                1L,
                "Test Subject",
                "Test Description",
                "OPEN",
                2L,
                "john@example.com",
                LocalDateTime.now()
        );

        when(ticketService.getAllTickets()).thenReturn(List.of(ticket));

        // When & Then
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].subject").value("Test Subject"));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenNotAuthenticatedForGetTickets() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void shouldCreateTicketWhenAuthenticated() throws Exception {
        // Given - FIXED: 7 parameters
        SupportTicketDTO createdTicket = new SupportTicketDTO(
                10L,
                "New Issue",
                "Description",
                "OPEN",
                2L,
                "john@example.com",
                LocalDateTime.now()
        );

        when(ticketService.createTicket(anyLong(), anyString(), anyString()))
                .thenReturn(createdTicket);

        String requestBody = """
                {
                    "userId": 2,
                    "subject": "New Issue",
                    "description": "Description"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("New Issue"));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenNotAuthenticatedForCreateTicket() throws Exception {
        // Given
        String requestBody = """
                {
                    "userId": 2,
                    "subject": "New Issue",
                    "description": "Description"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldUpdateTicketStatusWhenAuthenticatedAsAdmin() throws Exception {
        // Given - FIXED: 7 parameters
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
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}