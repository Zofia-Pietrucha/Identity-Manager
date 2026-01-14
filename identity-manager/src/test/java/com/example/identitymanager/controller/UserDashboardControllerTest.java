package com.example.identitymanager.controller;

import com.example.identitymanager.dto.SupportTicketDTO;
import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.service.FileStorageService;
import com.example.identitymanager.service.SupportTicketService;
import com.example.identitymanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserDashboardController.class)
@WithMockUser(username = "user@example.com", roles = {"USER"})
class UserDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private SupportTicketService ticketService;

    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    private UserDTO testUserDTO;
    private SupportTicketDTO testTicketDTO;

    @BeforeEach
    void setUp() {
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

        testTicketDTO = new SupportTicketDTO(
                1L,
                "Test Issue",
                "Test Description",
                "OPEN",
                1L,
                "user@example.com",
                LocalDateTime.now()
        );
    }

    // ==================== DASHBOARD TESTS ====================

    @Test
    void shouldShowDashboard() throws Exception {
        // Given
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        when(ticketService.getTicketsByUserEmail("user@example.com")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/dashboard"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("tickets"));

        verify(userService).getUserByEmail("user@example.com");
        verify(ticketService).getTicketsByUserEmail("user@example.com");
    }

    @Test
    void shouldShowDashboardWithTickets() throws Exception {
        // Given
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        when(ticketService.getTicketsByUserEmail("user@example.com"))
                .thenReturn(Arrays.asList(testTicketDTO));

        // When & Then
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/dashboard"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("tickets"));

        verify(ticketService).getTicketsByUserEmail("user@example.com");
    }

    @Test
    void shouldShowDashboardWithAvatar() throws Exception {
        // Given
        testUserDTO.setAvatarFilename("avatar.jpg");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        when(ticketService.getTicketsByUserEmail("user@example.com")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/dashboard"))
                .andExpect(model().attributeExists("user"));

        verify(userService).getUserByEmail("user@example.com");
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Test
    void shouldUpdateProfile() throws Exception {
        // Given
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        when(userService.updateUserProfile(anyString(), any(UserUpdateDTO.class))).thenReturn(testUserDTO);
        when(userService.updatePrivacySettings(anyString(), anyBoolean())).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(multipart("/user/profile/update")
                        .with(csrf())
                        .param("firstName", "Updated")
                        .param("lastName", "Name")
                        .param("phone", "987654321")
                        .param("isPrivacyEnabled", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard?success=true"));

        verify(userService).updateUserProfile(eq("user@example.com"), any(UserUpdateDTO.class));
        verify(userService).updatePrivacySettings("user@example.com", true);
    }

    @Test
    void shouldUpdateProfileWithAvatar() throws Exception {
        // Given
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        when(fileStorageService.storeFile(any())).thenReturn("new-avatar.jpg");
        when(userService.updateUserProfile(anyString(), any(UserUpdateDTO.class))).thenReturn(testUserDTO);
        when(userService.updatePrivacySettings(anyString(), anyBoolean())).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(multipart("/user/profile/update")
                        .file(avatarFile)
                        .with(csrf())
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("isPrivacyEnabled", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard?success=true"));

        verify(fileStorageService).storeFile(any());
        verify(userService).updateUserAvatar("user@example.com", "new-avatar.jpg");
    }

    @Test
    void shouldReplaceOldAvatarWhenUploadingNew() throws Exception {
        // Given
        testUserDTO.setAvatarFilename("old-avatar.jpg");
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "new-avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        when(fileStorageService.storeFile(any())).thenReturn("new-avatar.jpg");
        when(userService.updateUserProfile(anyString(), any(UserUpdateDTO.class))).thenReturn(testUserDTO);
        when(userService.updatePrivacySettings(anyString(), anyBoolean())).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(multipart("/user/profile/update")
                        .file(avatarFile)
                        .with(csrf())
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("isPrivacyEnabled", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard?success=true"));

        verify(fileStorageService).deleteFile("old-avatar.jpg");
        verify(fileStorageService).storeFile(any());
    }

    @Test
    void shouldShowValidationErrorWhenFirstNameInvalid() throws Exception {
        // Given
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));

        // When & Then
        mockMvc.perform(multipart("/user/profile/update")
                        .with(csrf())
                        .param("firstName", "")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(flash().attributeExists("error"));

        verify(userService, never()).updateUserProfile(anyString(), any(UserUpdateDTO.class));
    }

    // ==================== DELETE AVATAR TESTS ====================

    @Test
    void shouldDeleteAvatar() throws Exception {
        // Given
        testUserDTO.setAvatarFilename("avatar.jpg");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        doNothing().when(fileStorageService).deleteFile("avatar.jpg");

        // When & Then
        mockMvc.perform(post("/user/avatar/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard?success=true"));

        verify(fileStorageService).deleteFile("avatar.jpg");
        verify(userService).updateUserAvatar("user@example.com", null);
    }

    @Test
    void shouldNotDeleteAvatarWhenNoneExists() throws Exception {
        // Given
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));

        // When & Then
        mockMvc.perform(post("/user/avatar/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(userService, never()).updateUserAvatar(anyString(), any());
    }

    @Test
    void shouldReturnErrorWhenDeletingAvatarFails() throws Exception {
        // Given
        testUserDTO.setAvatarFilename("avatar.jpg");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        doThrow(new RuntimeException("Delete failed")).when(fileStorageService).deleteFile("avatar.jpg");

        // When & Then
        mockMvc.perform(post("/user/avatar/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== CREATE TICKET TESTS ====================

    @Test
    void shouldCreateTicket() throws Exception {
        // Given
        when(ticketService.createTicketForCurrentUser(eq("user@example.com"), eq("Test Subject"), eq("Test Description")))
                .thenReturn(testTicketDTO);

        // When & Then
        mockMvc.perform(post("/user/tickets")
                        .with(csrf())
                        .param("subject", "Test Subject")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(flash().attributeExists("ticketSuccess"));

        verify(ticketService).createTicketForCurrentUser("user@example.com", "Test Subject", "Test Description");
    }

    @Test
    void shouldShowErrorWhenSubjectIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/user/tickets")
                        .with(csrf())
                        .param("subject", "")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(flash().attributeExists("ticketError"));

        verify(ticketService, never()).createTicketForCurrentUser(any(), any(), any());
    }

    @Test
    void shouldShowErrorWhenDescriptionIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/user/tickets")
                        .with(csrf())
                        .param("subject", "Test Subject")
                        .param("description", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(flash().attributeExists("ticketError"));

        verify(ticketService, never()).createTicketForCurrentUser(any(), any(), any());
    }

    @Test
    void shouldShowErrorWhenSubjectIsOnlyWhitespace() throws Exception {
        // When & Then
        mockMvc.perform(post("/user/tickets")
                        .with(csrf())
                        .param("subject", "   ")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(flash().attributeExists("ticketError"));

        verify(ticketService, never()).createTicketForCurrentUser(any(), any(), any());
    }

    @Test
    void shouldShowErrorWhenTicketCreationFails() throws Exception {
        // Given
        when(ticketService.createTicketForCurrentUser(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/user/tickets")
                        .with(csrf())
                        .param("subject", "Test Subject")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(flash().attributeExists("ticketError"));
    }
}