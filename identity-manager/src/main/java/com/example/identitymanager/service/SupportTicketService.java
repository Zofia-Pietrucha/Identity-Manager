package com.example.identitymanager.service;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.SupportTicketRepository;
import com.example.identitymanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;

    public SupportTicketService(SupportTicketRepository ticketRepository,
                                UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // Get all tickets
    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get ticket by ID
    @Transactional(readOnly = true)
    public SupportTicketDTO getTicketById(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return convertToDTO(ticket);
    }

    // Get tickets by user ID
    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getTicketsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return ticketRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Create new ticket
    public SupportTicketDTO createTicket(Long userId, String subject, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        SupportTicket ticket = new SupportTicket();
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);
        ticket.setUser(user);

        SupportTicket savedTicket = ticketRepository.save(ticket);
        return convertToDTO(savedTicket);
    }

    // Update ticket status
    public SupportTicketDTO updateTicketStatus(Long id, SupportTicket.TicketStatus newStatus) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        ticket.setStatus(newStatus);
        SupportTicket updatedTicket = ticketRepository.save(ticket);
        return convertToDTO(updatedTicket);
    }

    // Convert entity to DTO
    private SupportTicketDTO convertToDTO(SupportTicket ticket) {
        return new SupportTicketDTO(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getUser().getId(),
                ticket.getUser().getEmail(),
                ticket.getCreatedAt()
        );
    }
}