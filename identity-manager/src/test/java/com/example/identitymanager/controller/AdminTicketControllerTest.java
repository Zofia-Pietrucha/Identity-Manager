package com.example.identitymanager.controller;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.repository.SupportTicketRepository;
import com.example.identitymanager.service.SupportTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTicketController.class)
@WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
class AdminTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupportTicketService ticketService;

    // Required for Spring Security context
    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    // Note: We don't need to mock SupportTicketRepository here because
    // AdminTicketController only uses SupportTicketService

    private SupportTicketDTO testTicketDTO;

    @BeforeEach
    void setUp() {
        testTicketDTO = new SupportTicketDTO(
                1L,
                "Test Issue",
                "Test Description",
                "OPEN",
                1L,
                "user@example.com",
                LocalDateTime.now()
        );
    }

    // ==================== LIST TICKETS TESTS ====================

    @Test
    void shouldShowTicketsList() throws Exception {
        // Given - controller uses ticketService.getAllTickets(), returns List<SupportTicketDTO>
        List<SupportTicketDTO> tickets = Collections.singletonList(testTicketDTO);
        when(ticketService.getAllTickets()).thenReturn(tickets);

        // When & Then
        mockMvc.perform(get("/admin/tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tickets-list"))
                .andExpect(model().attributeExists("tickets"));

        verify(ticketService).getAllTickets();
    }

    @Test
    void shouldShowEmptyTicketsList() throws Exception {
        // Given
        when(ticketService.getAllTickets()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/admin/tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tickets-list"))
                .andExpect(model().attributeExists("tickets"));

        verify(ticketService).getAllTickets();
    }

    // ==================== VIEW TICKET DETAIL TESTS ====================

    @Test
    void shouldShowTicketDetail() throws Exception {
        // Given - controller uses ticketService.getTicketById()
        when(ticketService.getTicketById(1L)).thenReturn(testTicketDTO);

        // When & Then
        mockMvc.perform(get("/admin/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ticket-detail"))
                .andExpect(model().attributeExists("ticket"));

        verify(ticketService).getTicketById(1L);
    }

    @Test
    void shouldRedirectWhenTicketNotFound() throws Exception {
        // Given - service throws exception when ticket not found
        when(ticketService.getTicketById(999L))
                .thenThrow(new ResourceNotFoundException("SupportTicket", "id", 999L));

        // When & Then - controller catches exception and redirects with flash error
        mockMvc.perform(get("/admin/tickets/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== UPDATE TICKET STATUS TESTS ====================

    @Test
    void shouldUpdateTicketStatus() throws Exception {
        // Given - updateTicketStatus returns SupportTicketDTO, not void
        when(ticketService.updateTicketStatus(eq(1L), eq(SupportTicket.TicketStatus.RESOLVED)))
                .thenReturn(testTicketDTO);

        // When & Then - redirects back to ticket detail page
        mockMvc.perform(post("/admin/tickets/1/status")
                        .with(csrf())
                        .param("status", "RESOLVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets/1"))
                .andExpect(flash().attributeExists("success"));

        verify(ticketService).updateTicketStatus(1L, SupportTicket.TicketStatus.RESOLVED);
    }

    @Test
    void shouldUpdateTicketStatusToInProgress() throws Exception {
        // Given
        when(ticketService.updateTicketStatus(eq(1L), eq(SupportTicket.TicketStatus.IN_PROGRESS)))
                .thenReturn(testTicketDTO);

        // When & Then
        mockMvc.perform(post("/admin/tickets/1/status")
                        .with(csrf())
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets/1"))
                .andExpect(flash().attributeExists("success"));

        verify(ticketService).updateTicketStatus(1L, SupportTicket.TicketStatus.IN_PROGRESS);
    }

    @Test
    void shouldUpdateTicketStatusToClosed() throws Exception {
        // Given
        when(ticketService.updateTicketStatus(eq(1L), eq(SupportTicket.TicketStatus.CLOSED)))
                .thenReturn(testTicketDTO);

        // When & Then
        mockMvc.perform(post("/admin/tickets/1/status")
                        .with(csrf())
                        .param("status", "CLOSED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets/1"))
                .andExpect(flash().attributeExists("success"));

        verify(ticketService).updateTicketStatus(1L, SupportTicket.TicketStatus.CLOSED);
    }

    @Test
    void shouldReturnErrorWhenUpdatingWithInvalidStatus() throws Exception {
        // Given - no mocking needed, controller catches IllegalArgumentException for invalid enum value

        // When & Then - redirects with error for invalid status
        mockMvc.perform(post("/admin/tickets/1/status")
                        .with(csrf())
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets/1"))
                .andExpect(flash().attributeExists("error"));

        verify(ticketService, never()).updateTicketStatus(anyLong(), any());
    }

    @Test
    void shouldReturnErrorWhenUpdatingStatusOfNonExistentTicket() throws Exception {
        // Given - service throws exception when ticket not found
        doThrow(new ResourceNotFoundException("SupportTicket", "id", 999L))
                .when(ticketService).updateTicketStatus(eq(999L), any());

        // When & Then - redirects to ticket detail page (same id), with error
        mockMvc.perform(post("/admin/tickets/999/status")
                        .with(csrf())
                        .param("status", "RESOLVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets/999"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void shouldHandleServiceExceptionWhenUpdatingStatus() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(ticketService).updateTicketStatus(eq(1L), any());

        // When & Then
        mockMvc.perform(post("/admin/tickets/1/status")
                        .with(csrf())
                        .param("status", "RESOLVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets/1"))
                .andExpect(flash().attributeExists("error"));
    }
}