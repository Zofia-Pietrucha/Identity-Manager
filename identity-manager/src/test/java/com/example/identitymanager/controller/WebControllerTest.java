package com.example.identitymanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Need to mock CustomUserDetailsService for security context
    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    // ==================== ACCESS DENIED PAGE TESTS ====================

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldShowAccessDeniedPage() throws Exception {
        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("403"));
    }

    // ==================== ROOT REDIRECT TESTS ====================

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldRedirectAdminToAdminUsers() throws Exception {
        // Controller checks if user is admin and redirects to /admin/users
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void shouldRedirectUserToUserDashboard() throws Exception {
        // Controller checks if user is not admin and redirects to /user/dashboard
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER", "ADMIN"})
    void shouldRedirectUserWithBothRolesToAdminUsers() throws Exception {
        // User with both roles should be treated as admin (has ROLE_ADMIN)
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }
}