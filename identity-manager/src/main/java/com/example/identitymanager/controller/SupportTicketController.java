package com.example.identitymanager.controller;

import com.example.identitymanager.dto.CreateTicketRequest;
import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.dto.UpdateTicketStatusRequest;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Support Tickets", description = "APIs for managing support tickets")
public class SupportTicketController {

    private final SupportTicketService ticketService;

    public SupportTicketController(SupportTicketService ticketService) {
        this.ticketService = ticketService;
    }

    // GET /api/tickets - Get all tickets
    @GetMapping
    @Operation(summary = "Get all support tickets", description = "Retrieves a list of all support tickets")
    public ResponseEntity<List<SupportTicketDTO>> getAllTickets() {
        List<SupportTicketDTO> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    // GET /api/tickets/{id} - Get ticket by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", description = "Retrieves a specific support ticket by its ID")
    public ResponseEntity<SupportTicketDTO> getTicketById(@PathVariable Long id) {
        SupportTicketDTO ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    // GET /api/tickets/user/{userId} - Get tickets by user
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get tickets by user", description = "Retrieves all tickets for a specific user")
    public ResponseEntity<List<SupportTicketDTO>> getTicketsByUserId(@PathVariable Long userId) {
        List<SupportTicketDTO> tickets = ticketService.getTicketsByUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    // POST /api/tickets - Create new ticket
    @PostMapping
    @Operation(summary = "Create a new support ticket", description = "Creates a new support ticket for a user")
    public ResponseEntity<SupportTicketDTO> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        SupportTicketDTO createdTicket = ticketService.createTicket(
                request.getUserId(),
                request.getSubject(),
                request.getDescription()
        );
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    // PATCH /api/tickets/{id}/status - Update ticket status
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ticket status", description = "Updates the status of an existing support ticket")
    public ResponseEntity<SupportTicketDTO> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {

        // Convert string to enum
        SupportTicket.TicketStatus newStatus;
        try {
            newStatus = SupportTicket.TicketStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: OPEN, IN_PROGRESS, RESOLVED, CLOSED");
        }

        SupportTicketDTO updatedTicket = ticketService.updateTicketStatus(id, newStatus);
        return ResponseEntity.ok(updatedTicket);
    }
}