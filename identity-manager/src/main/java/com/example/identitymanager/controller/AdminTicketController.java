package com.example.identitymanager.controller;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.service.SupportTicketService;
import com.example.identitymanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/tickets")
public class AdminTicketController {

    private final SupportTicketService ticketService;
    private final UserService userService;

    public AdminTicketController(SupportTicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    // GET /admin/tickets - List all support tickets
    @GetMapping
    public String listTickets(Model model) {
        List<SupportTicketDTO> tickets = ticketService.getAllTickets();
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("tickets", tickets);
        model.addAttribute("users", users);
        return "admin/tickets-list";
    }

    // GET /admin/tickets/{id} - View ticket details
    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            SupportTicketDTO ticket = ticketService.getTicketById(id);
            model.addAttribute("ticket", ticket);
            return "admin/ticket-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ticket not found: " + e.getMessage());
            return "redirect:/admin/tickets";
        }
    }

    // POST /admin/tickets - Create new ticket (admin can assign to any user)
    @PostMapping
    public String createTicket(@RequestParam("userId") Long userId,
                               @RequestParam("subject") String subject,
                               @RequestParam("description") String description,
                               RedirectAttributes redirectAttributes) {

        // Walidacja
        if (subject == null || subject.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Subject is required");
            return "redirect:/admin/tickets";
        }
        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Description is required");
            return "redirect:/admin/tickets";
        }

        try {
            ticketService.createTicket(userId, subject.trim(), description.trim());
            redirectAttributes.addFlashAttribute("success", "Ticket created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create ticket: " + e.getMessage());
        }

        return "redirect:/admin/tickets";
    }

    // POST /admin/tickets/{id}/status - Update ticket status
    @PostMapping("/{id}/status")
    public String updateTicketStatus(
            @PathVariable Long id,
            @RequestParam("status") String statusStr,
            RedirectAttributes redirectAttributes) {
        try {
            SupportTicket.TicketStatus newStatus = SupportTicket.TicketStatus.valueOf(statusStr.toUpperCase());
            ticketService.updateTicketStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("success", "Ticket status updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid status value");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating ticket: " + e.getMessage());
        }
        return "redirect:/admin/tickets/" + id;
    }

    // GET /admin/tickets/{id}/delete - Delete ticket
    @GetMapping("/{id}/delete")
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete ticket: " + e.getMessage());
        }
        return "redirect:/admin/tickets";
    }
}