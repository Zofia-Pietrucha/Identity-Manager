package com.example.identitymanager.service;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.SupportTicketRepository;
import com.example.identitymanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SupportTicketService ticketService;

    private User testUser;
    private SupportTicket testTicket;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testTicket = new SupportTicket();
        testTicket.setId(1L);
        testTicket.setSubject("Test Issue");
        testTicket.setDescription("Test Description");
        testTicket.setStatus(SupportTicket.TicketStatus.OPEN);
        testTicket.setUser(testUser);
        testTicket.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldGetAllTickets() {
        // Given
        when(ticketRepository.findAll()).thenReturn(Arrays.asList(testTicket));

        // When
        List<SupportTicketDTO> result = ticketService.getAllTickets();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubject()).isEqualTo("Test Issue");
        verify(ticketRepository).findAll();
    }

    @Test
    void shouldGetTicketById() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // When
        SupportTicketDTO result = ticketService.getTicketById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSubject()).isEqualTo("Test Issue");
        verify(ticketRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTicketNotFound() {
        // Given
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.getTicketById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(ticketRepository).findById(999L);
    }

    @Test
    void shouldCreateTicket() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);

        // When
        SupportTicketDTO result = ticketService.createTicket(1L, "New Issue", "Description");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo("Test Issue");
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(ticketRepository).save(any(SupportTicket.class));
    }

    @Test
    void shouldSetStatusToOpenWhenCreatingTicket() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ticketService.createTicket(1L, "Subject", "Description");

        // Then
        verify(ticketRepository).save(argThat(ticket ->
                ticket.getStatus() == SupportTicket.TicketStatus.OPEN
        ));
    }

    @Test
    void shouldUpdateTicketStatus() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> {
            SupportTicket ticket = invocation.getArgument(0);
            return ticket;
        });

        // When
        SupportTicketDTO result = ticketService.updateTicketStatus(1L, SupportTicket.TicketStatus.RESOLVED);

        // Then
        assertThat(result.getStatus()).isEqualTo("RESOLVED");
        verify(ticketRepository).save(argThat(ticket ->
                ticket.getStatus() == SupportTicket.TicketStatus.RESOLVED
        ));
    }

    @Test
    void shouldGetTicketsByUserId() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepository.findByUser(testUser)).thenReturn(Arrays.asList(testTicket));

        // When
        List<SupportTicketDTO> result = ticketService.getTicketsByUserId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        verify(ticketRepository).findByUser(testUser);
    }

    @Test
    void shouldConvertTicketToDTO() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // When
        SupportTicketDTO dto = ticketService.getTicketById(1L);

        // Then
        assertThat(dto.getId()).isEqualTo(testTicket.getId());
        assertThat(dto.getSubject()).isEqualTo(testTicket.getSubject());
        assertThat(dto.getDescription()).isEqualTo(testTicket.getDescription());
        assertThat(dto.getStatus()).isEqualTo(testTicket.getStatus().name());
        assertThat(dto.getUserId()).isEqualTo(testTicket.getUser().getId());
        assertThat(dto.getUserEmail()).isEqualTo(testTicket.getUser().getEmail());
    }
}