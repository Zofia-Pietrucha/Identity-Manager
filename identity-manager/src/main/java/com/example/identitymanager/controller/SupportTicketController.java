package com.example.identitymanager.controller;

import com.example.identitymanager.dto.CreateTicketRequest;
import com.example.identitymanager.dto.CreateTicketRequestUser;
import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.dto.UpdateTicketStatusRequest;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    // GET /api/tickets - ADMIN gets all tickets, USER gets only their own
    @GetMapping
    @Operation(summary = "Get tickets", description = "ADMIN gets all tickets, USER gets only their own tickets")
    public ResponseEntity<List<SupportTicketDTO>> getTickets(Authentication authentication) {
        if (isAdmin(authentication)) {
            // Admin widzi wszystkie tickety
            List<SupportTicketDTO> tickets = ticketService.getAllTickets();
            return ResponseEntity.ok(tickets);
        } else {
            // User widzi tylko swoje tickety
            String email = authentication.getName();
            List<SupportTicketDTO> tickets = ticketService.getTicketsByUserEmail(email);
            return ResponseEntity.ok(tickets);
        }
    }

    // GET /api/tickets/my - Get current user's tickets
    @GetMapping("/my")
    @Operation(summary = "Get my tickets", description = "Retrieves all tickets for the currently logged-in user")
    public ResponseEntity<List<SupportTicketDTO>> getMyTickets(Authentication authentication) {
        String email = authentication.getName();
        List<SupportTicketDTO> tickets = ticketService.getTicketsByUserEmail(email);
        return ResponseEntity.ok(tickets);
    }

    // GET /api/tickets/{id} - Get ticket by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", description = "Retrieves a specific support ticket by its ID")
    public ResponseEntity<SupportTicketDTO> getTicketById(@PathVariable Long id, Authentication authentication) {
        SupportTicketDTO ticket = ticketService.getTicketById(id);

        // User może zobaczyć tylko swój ticket, Admin może zobaczyć każdy
        if (!isAdmin(authentication) && !ticket.getUserEmail().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(ticket);
    }

    // GET /api/tickets/user/{userId} - Get tickets by user (ADMIN only)
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get tickets by user (ADMIN only)", description = "Retrieves all tickets for a specific user - requires ADMIN role")
    public ResponseEntity<List<SupportTicketDTO>> getTicketsByUserId(
            @PathVariable Long userId,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SupportTicketDTO> tickets = ticketService.getTicketsByUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    // POST /api/tickets - Create new ticket (USER creates for self, ADMIN can specify userId)
    @PostMapping
    @Operation(summary = "Create a new support ticket", description = "USER creates ticket for themselves, ADMIN can create for any user")
    public ResponseEntity<SupportTicketDTO> createTicket(
            @Valid @RequestBody CreateTicketRequestUser request,
            Authentication authentication) {

        // User tworzy ticket tylko na siebie
        String email = authentication.getName();
        SupportTicketDTO createdTicket = ticketService.createTicketForCurrentUser(
                email,
                request.getSubject(),
                request.getDescription()
        );
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    // POST /api/tickets/admin - Create ticket for specific user (ADMIN only)
    @PostMapping("/admin")
    @Operation(summary = "Create ticket for specific user (ADMIN only)", description = "Creates a new support ticket for a specified user - requires ADMIN role")
    public ResponseEntity<SupportTicketDTO> createTicketAsAdmin(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SupportTicketDTO createdTicket = ticketService.createTicket(
                request.getUserId(),
                request.getSubject(),
                request.getDescription()
        );
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    // PATCH /api/tickets/{id}/status - Update ticket status (ADMIN only)
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ticket status (ADMIN only)", description = "Updates the status of an existing support ticket - requires ADMIN role")
    public ResponseEntity<SupportTicketDTO> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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

    // Helper method to check if user has ADMIN role
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}