package com.example.identitymanager.controller;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.service.SupportTicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/tickets")
public class AdminTicketController {

    private final SupportTicketService ticketService;

    public AdminTicketController(SupportTicketService ticketService) {
        this.ticketService = ticketService;
    }

    // GET /admin/tickets - List all support tickets
    @GetMapping
    public String listTickets(Model model) {
        List<SupportTicketDTO> tickets = ticketService.getAllTickets();
        model.addAttribute("tickets", tickets);
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
}