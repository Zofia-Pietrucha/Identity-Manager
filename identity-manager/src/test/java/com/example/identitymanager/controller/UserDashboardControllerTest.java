package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.service.FileStorageService;
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

    private UserDTO testUserDTO;

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
    }

    @Test
    void shouldShowDashboard() throws Exception {
        // Given
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));

        // When & Then
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/dashboard"))
                .andExpect(model().attributeExists("user"));

        verify(userService).getUserByEmail("user@example.com");
    }

    @Test
    void shouldShowDashboardWithAvatar() throws Exception {
        // Given
        testUserDTO.setAvatarFilename("avatar.jpg");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));

        // When & Then
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/dashboard"))
                .andExpect(model().attributeExists("user"));

        verify(userService).getUserByEmail("user@example.com");
    }

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
    void shouldReplaceExistingAvatar() throws Exception {
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
        doNothing().when(fileStorageService).deleteFile("old-avatar.jpg");
        when(userService.updateUserProfile(anyString(), any(UserUpdateDTO.class))).thenReturn(testUserDTO);
        when(userService.updatePrivacySettings(anyString(), anyBoolean())).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(multipart("/user/profile/update")
                        .file(avatarFile)
                        .with(csrf())
                        .param("firstName", "Test")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection());

        verify(fileStorageService).deleteFile("old-avatar.jpg");
        verify(fileStorageService).storeFile(any());
    }

    @Test
    void shouldReturnErrorWhenAvatarUploadFails() throws Exception {
        // Given
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUserDTO));
        when(fileStorageService.storeFile(any())).thenThrow(new RuntimeException("Upload failed"));

        // When & Then
        mockMvc.perform(multipart("/user/profile/update")
                        .file(avatarFile)
                        .with(csrf())
                        .param("firstName", "Test")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(flash().attributeExists("error"));

        verify(userService, never()).updateUserProfile(anyString(), any(UserUpdateDTO.class));
    }

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
}