package com.example.identitymanager.controller;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.service.SupportTicketService;
import com.example.identitymanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
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

    @MockBean
    private UserService userService;

    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    private SupportTicketDTO testTicketDTO;
    private UserDTO testUserDTO;

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

        testUserDTO = new UserDTO(
                1L,
                "user@example.com",
                "Test",
                "User",
                "123456789",
                false,
                Set.of("USER"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }

    // ==================== LIST TICKETS TESTS ====================

    @Test
    void shouldShowTicketsList() throws Exception {
        // Given
        List<SupportTicketDTO> tickets = Collections.singletonList(testTicketDTO);
        List<UserDTO> users = Collections.singletonList(testUserDTO);
        when(ticketService.getAllTickets()).thenReturn(tickets);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/admin/tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tickets-list"))
                .andExpect(model().attributeExists("tickets"))
                .andExpect(model().attributeExists("users"));

        verify(ticketService).getAllTickets();
        verify(userService).getAllUsers();
    }

    @Test
    void shouldShowEmptyTicketsList() throws Exception {
        // Given
        when(ticketService.getAllTickets()).thenReturn(Collections.emptyList());
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(testUserDTO));

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
        // Given
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
        // Given
        when(ticketService.getTicketById(999L))
                .thenThrow(new ResourceNotFoundException("SupportTicket", "id", 999L));

        // When & Then
        mockMvc.perform(get("/admin/tickets/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== CREATE TICKET TESTS ====================

    @Test
    void shouldCreateTicket() throws Exception {
        // Given
        when(ticketService.createTicket(eq(1L), eq("Test Subject"), eq("Test Description")))
                .thenReturn(testTicketDTO);

        // When & Then
        mockMvc.perform(post("/admin/tickets")
                        .with(csrf())
                        .param("userId", "1")
                        .param("subject", "Test Subject")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(flash().attributeExists("success"));

        verify(ticketService).createTicket(1L, "Test Subject", "Test Description");
    }

    @Test
    void shouldShowErrorWhenSubjectIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/tickets")
                        .with(csrf())
                        .param("userId", "1")
                        .param("subject", "")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(flash().attributeExists("error"));

        verify(ticketService, never()).createTicket(any(), any(), any());
    }

    @Test
    void shouldShowErrorWhenDescriptionIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/tickets")
                        .with(csrf())
                        .param("userId", "1")
                        .param("subject", "Test Subject")
                        .param("description", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(flash().attributeExists("error"));

        verify(ticketService, never()).createTicket(any(), any(), any());
    }

    @Test
    void shouldShowErrorWhenUserNotFound() throws Exception {
        // Given
        when(ticketService.createTicket(eq(999L), anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("User", "id", 999L));

        // When & Then
        mockMvc.perform(post("/admin/tickets")
                        .with(csrf())
                        .param("userId", "999")
                        .param("subject", "Test Subject")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== UPDATE TICKET STATUS TESTS ====================

    @Test
    void shouldUpdateTicketStatus() throws Exception {
        // Given
        when(ticketService.updateTicketStatus(eq(1L), eq(SupportTicket.TicketStatus.RESOLVED)))
                .thenReturn(testTicketDTO);

        // When & Then
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
    void shouldShowErrorForInvalidStatus() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/tickets/1/status")
                        .with(csrf())
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets/1"))
                .andExpect(flash().attributeExists("error"));

        verify(ticketService, never()).updateTicketStatus(any(), any());
    }

    // ==================== DELETE TICKET TESTS ====================

    @Test
    void shouldDeleteTicket() throws Exception {
        // Given
        doNothing().when(ticketService).deleteTicket(1L);

        // When & Then
        mockMvc.perform(get("/admin/tickets/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(flash().attributeExists("success"));

        verify(ticketService).deleteTicket(1L);
    }

    @Test
    void shouldShowErrorWhenDeletingNonExistentTicket() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Ticket", "id", 999L))
                .when(ticketService).deleteTicket(999L);

        // When & Then
        mockMvc.perform(get("/admin/tickets/999/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tickets"))
                .andExpect(flash().attributeExists("error"));
    }
}