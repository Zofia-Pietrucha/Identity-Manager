package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // GET /admin/users - List all users with pagination
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // For now, get all users (we'll add pagination in UserService later)
        List<UserDTO> allUsers = userService.getAllUsers();

        // Simple pagination simulation
        int totalUsers = allUsers.size();
        int totalPages = (int) Math.ceil((double) totalUsers / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalUsers);

        List<UserDTO> pageUsers = allUsers.subList(startIndex, endIndex);

        model.addAttribute("users", pageUsers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);

        return "admin/users-list";
    }
}