package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.UserRepository;
import com.example.identitymanager.service.FileStorageService;
import com.example.identitymanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerAdditionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private UserRepository userRepository;

    // Required for Spring Security context
    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        HashSet<String> roles = new HashSet<>();
        roles.add("USER");

        userDTO = new UserDTO(
                1L,
                "test@example.com",
                "Test",
                "User",
                "123456789",
                false,
                roles,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "avatar.jpg",
                "/api/users/1/avatar"
        );

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAvatarFilename("avatar.jpg");
    }

    // ==================== PAGINATED USERS TESTS ====================

    @Test
    @WithMockUser
    void shouldGetUsersPaginated() throws Exception {
        // Given
        Page<UserDTO> userPage = new PageImpl<>(Collections.singletonList(userDTO));
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        // When & Then - Note: response uses "totalItems" not "totalElements"
        mockMvc.perform(get("/api/users/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalItems", is(1)));  // Changed from totalElements

        verify(userService).getAllUsers(any(PageRequest.class));
    }

    @Test
    @WithMockUser
    void shouldGetUsersPaginatedWithDefaultParams() throws Exception {
        // Given
        Page<UserDTO> userPage = new PageImpl<>(Collections.singletonList(userDTO));
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        // When & Then - using default params
        mockMvc.perform(get("/api/users/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.currentPage", is(0)));

        verify(userService).getAllUsers(any(PageRequest.class));
    }

    // ==================== SEARCH USERS TESTS ====================

    @Test
    @WithMockUser
    void shouldSearchUsers() throws Exception {
        // Given
        Page<UserDTO> userPage = new PageImpl<>(Collections.singletonList(userDTO));
        when(userService.searchUsers(anyString(), any(PageRequest.class))).thenReturn(userPage);

        // When & Then - Note: response uses "totalItems" not "totalElements", no "keyword" field
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(1)));

        verify(userService).searchUsers(eq("test"), any(PageRequest.class));
    }

    // ==================== GET USERS BY ROLE TESTS ====================

    @Test
    @WithMockUser
    void shouldGetUsersByRole() throws Exception {
        // Given - Note: endpoint is /by-role/{roleName} not /role/{roleName}
        when(userService.getUsersByRole("USER")).thenReturn(Collections.singletonList(userDTO));

        // When & Then
        mockMvc.perform(get("/api/users/by-role/USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("test@example.com")));

        verify(userService).getUsersByRole("USER");
    }

    @Test
    @WithMockUser
    void shouldGetAdminUsers() throws Exception {
        // Given
        userDTO = new UserDTO(1L, "admin@example.com", "Admin", "User", null, false,
                Set.of("ADMIN"), LocalDateTime.now(), LocalDateTime.now(), null, null);
        when(userService.getUsersByRole("ADMIN")).thenReturn(Collections.singletonList(userDTO));

        // When & Then
        mockMvc.perform(get("/api/users/by-role/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("admin@example.com")));

        verify(userService).getUsersByRole("ADMIN");
    }

    @Test
    @WithMockUser
    void shouldReturnErrorForInvalidRole() throws Exception {
        // Given
        when(userService.getUsersByRole("INVALID"))
                .thenThrow(new IllegalArgumentException("Invalid role name: INVALID"));

        // When & Then
        mockMvc.perform(get("/api/users/by-role/INVALID"))
                .andExpect(status().isBadRequest());
    }

    // ==================== PRIVACY STATS TESTS ====================

    @Test
    @WithMockUser
    void shouldGetPrivacyStats() throws Exception {
        // Given
        when(userService.countUsersWithPrivacyEnabled()).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/users/stats/privacy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usersWithPrivacyEnabled", is(5)));

        verify(userService).countUsersWithPrivacyEnabled();
    }

    // ==================== UPDATE USER TESTS ====================

    @Test
    @WithMockUser
    void shouldUpdateUser() throws Exception {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated", "Name", "999888777");
        userDTO.setFirstName("Updated");
        userDTO.setLastName("Name");

        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class))).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("Name")));

        verify(userService).updateUser(eq(1L), any(UserUpdateDTO.class));
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO("Test", "User", "123456789");
        when(userService.updateUser(eq(999L), any(UserUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("User", "id", 999L));

        // When & Then
        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    @WithMockUser
    void shouldDeleteUser() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("User", "id", 999L))
                .when(userService).deleteUser(999L);

        // When & Then
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== AVATAR UPLOAD TESTS ====================

    @Test
    @WithMockUser
    void shouldUploadAvatar() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.storeFile(any())).thenReturn("avatar.jpg");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(multipart("/api/users/1/avatar")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Avatar uploaded successfully")))
                .andExpect(jsonPath("$.filename", is("avatar.jpg")));

        verify(fileStorageService).storeFile(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser
    void shouldReturnErrorWhenUploadingEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When & Then - controller throws IllegalArgumentException for empty file
        mockMvc.perform(multipart("/api/users/1/avatar")
                        .file(emptyFile))
                .andExpect(status().isBadRequest());

        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    @WithMockUser
    void shouldReturnErrorWhenUploadingNonImageFile() throws Exception {
        // Given
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "test content".getBytes()
        );

        // When & Then - controller validates content type before accessing repository
        mockMvc.perform(multipart("/api/users/1/avatar")
                        .file(textFile))
                .andExpect(status().isBadRequest());

        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    @WithMockUser
    void shouldReplaceExistingAvatar() throws Exception {
        // Given
        user.setAvatarFilename("old-avatar.jpg");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new-avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.storeFile(any())).thenReturn("new-avatar.jpg");
        doNothing().when(fileStorageService).deleteFile("old-avatar.jpg");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(multipart("/api/users/1/avatar")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename", is("new-avatar.jpg")));

        verify(fileStorageService).deleteFile("old-avatar.jpg");
        verify(fileStorageService).storeFile(any());
    }

    // ==================== AVATAR DOWNLOAD TESTS ====================

    @Test
    @WithMockUser
    void shouldGetAvatar() throws Exception {
        // Given
        byte[] imageBytes = "test image".getBytes();
        Resource resource = new ByteArrayResource(imageBytes);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.loadFileAsResource("avatar.jpg")).thenReturn(resource);

        // When & Then
        mockMvc.perform(get("/api/users/1/avatar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageBytes));

        verify(fileStorageService).loadFileAsResource("avatar.jpg");
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenAvatarNotFound() throws Exception {
        // Given - user has no avatar
        user.setAvatarFilename(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(get("/api/users/1/avatar"))
                .andExpect(status().isNotFound());

        verify(fileStorageService, never()).loadFileAsResource(any());
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenUserNotFoundForAvatar() throws Exception {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999/avatar"))
                .andExpect(status().isNotFound());
    }

    // ==================== AVATAR DELETE TESTS ====================

    @Test
    @WithMockUser
    void shouldDeleteAvatar() throws Exception {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(fileStorageService).deleteFile("avatar.jpg");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(delete("/api/users/1/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Avatar deleted successfully")));

        verify(fileStorageService).deleteFile("avatar.jpg");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenDeletingNonExistentAvatar() throws Exception {
        // Given - user has no avatar
        user.setAvatarFilename(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(delete("/api/users/1/avatar"))
                .andExpect(status().isNotFound());

        verify(fileStorageService, never()).deleteFile(any());
    }
}