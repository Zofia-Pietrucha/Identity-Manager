package com.example.identitymanager.controller;

import com.example.identitymanager.dto.CreateTicketRequest;
import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.dto.UpdateTicketStatusRequest;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(SupportTicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class SupportTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupportTicketService ticketService;

    private SupportTicketDTO ticketDTO;

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
    }

    @Test
    @WithMockUser
    void shouldGetAllTickets() throws Exception {
        // Given
        when(ticketService.getAllTickets()).thenReturn(Arrays.asList(ticketDTO));

        // When & Then
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].subject", is("Test Issue")));

        verify(ticketService).getAllTickets();
    }

    @Test
    @WithMockUser
    void shouldGetTicketById() throws Exception {
        // Given
        when(ticketService.getTicketById(1L)).thenReturn(ticketDTO);

        // When & Then
        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.subject", is("Test Issue")));

        verify(ticketService).getTicketById(1L);
    }

    @Test
    @WithMockUser
    void shouldCreateTicket() throws Exception {
        // Given
        CreateTicketRequest request = new CreateTicketRequest(
                1L,
                "New Issue",
                "New Description"
        );
        when(ticketService.createTicket(eq(1L), eq("New Issue"), eq("New Description")))
                .thenReturn(ticketDTO);

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject", is("Test Issue")));

        verify(ticketService).createTicket(1L, "New Issue", "New Description");
    }

    @Test
    @WithMockUser
    void shouldUpdateTicketStatus() throws Exception {
        // Given
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest("RESOLVED");
        SupportTicketDTO updatedTicket = new SupportTicketDTO(
                1L,
                "Test Issue",
                "Test Description",
                "RESOLVED",
                1L,
                "user@example.com",
                LocalDateTime.now()
        );
        when(ticketService.updateTicketStatus(eq(1L), any(SupportTicket.TicketStatus.class)))
                .thenReturn(updatedTicket);

        // When & Then
        mockMvc.perform(patch("/api/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));

        verify(ticketService).updateTicketStatus(eq(1L), eq(SupportTicket.TicketStatus.RESOLVED));
    }

    @Test
    @WithMockUser
    void shouldReturnValidationErrorWhenSubjectIsBlank() throws Exception {
        // Given
        CreateTicketRequest request = new CreateTicketRequest(
                1L,
                "",  // Empty subject
                "Description"
        );

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.subject").exists());

        verify(ticketService, never()).createTicket(any(), any(), any());
    }
}