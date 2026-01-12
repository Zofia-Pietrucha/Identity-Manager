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
import java.util.Collections;
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

    // ==================== GET ALL TICKETS TESTS ====================

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
    void shouldReturnEmptyListWhenNoTickets() {
        // Given
        when(ticketRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<SupportTicketDTO> result = ticketService.getAllTickets();

        // Then
        assertThat(result).isEmpty();
        verify(ticketRepository).findAll();
    }

    @Test
    void shouldGetMultipleTickets() {
        // Given
        SupportTicket ticket2 = new SupportTicket();
        ticket2.setId(2L);
        ticket2.setSubject("Second Issue");
        ticket2.setDescription("Second Description");
        ticket2.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        ticket2.setUser(testUser);
        ticket2.setCreatedAt(LocalDateTime.now());

        when(ticketRepository.findAll()).thenReturn(Arrays.asList(testTicket, ticket2));

        // When
        List<SupportTicketDTO> result = ticketService.getAllTickets();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSubject()).isEqualTo("Test Issue");
        assertThat(result.get(1).getSubject()).isEqualTo("Second Issue");

        verify(ticketRepository).findAll();
    }

    // ==================== GET TICKET BY ID TESTS ====================

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

    // ==================== GET TICKETS BY USER ID TESTS ====================

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
    void shouldReturnEmptyListWhenUserHasNoTickets() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        // When
        List<SupportTicketDTO> result = ticketService.getTicketsByUserId(1L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(1L);
        verify(ticketRepository).findByUser(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForTickets() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.getTicketsByUserId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(ticketRepository, never()).findByUser(any());
    }

    // ==================== CREATE TICKET TESTS ====================

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
    void shouldSetCorrectSubjectAndDescriptionWhenCreating() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ticketService.createTicket(1L, "My Subject", "My Description");

        // Then
        verify(ticketRepository).save(argThat(ticket ->
                "My Subject".equals(ticket.getSubject()) &&
                        "My Description".equals(ticket.getDescription())
        ));
    }

    @Test
    void shouldAssignUserToTicketWhenCreating() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ticketService.createTicket(1L, "Subject", "Description");

        // Then
        verify(ticketRepository).save(argThat(ticket ->
                ticket.getUser() != null && ticket.getUser().getId().equals(1L)
        ));
    }

    @Test
    void shouldThrowExceptionWhenCreatingTicketForNonExistentUser() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.createTicket(999L, "Subject", "Description"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(ticketRepository, never()).save(any());
    }

    // ==================== UPDATE TICKET STATUS TESTS ====================

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
    void shouldUpdateTicketStatusToInProgress() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SupportTicketDTO result = ticketService.updateTicketStatus(1L, SupportTicket.TicketStatus.IN_PROGRESS);

        // Then
        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(any(SupportTicket.class));
    }

    @Test
    void shouldUpdateTicketStatusToClosed() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SupportTicketDTO result = ticketService.updateTicketStatus(1L, SupportTicket.TicketStatus.CLOSED);

        // Then
        assertThat(result.getStatus()).isEqualTo("CLOSED");
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(any(SupportTicket.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusOfNonExistentTicket() {
        // Given
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.updateTicketStatus(999L, SupportTicket.TicketStatus.RESOLVED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(ticketRepository, never()).save(any());
    }

    // ==================== DTO CONVERSION TESTS ====================

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

        verify(ticketRepository).findById(1L);
    }

    @Test
    void shouldIncludeCreatedAtInDTO() {
        // Given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        testTicket.setCreatedAt(createdAt);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // When
        SupportTicketDTO dto = ticketService.getTicketById(1L);

        // Then
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        verify(ticketRepository).findById(1L);
    }
}