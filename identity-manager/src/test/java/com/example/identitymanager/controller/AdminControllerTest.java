package com.example.identitymanager.controller;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserRegistrationDTO;
import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.RoleRepository;
import com.example.identitymanager.repository.UserDao;
import com.example.identitymanager.repository.UserRepository;
import com.example.identitymanager.service.FileStorageService;
import com.example.identitymanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserDao userDao;

    @MockBean
    private FileStorageService fileStorageService;

    // Required for Spring Security context
    @MockBean
    private com.example.identitymanager.service.CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private UserDTO testUserDTO;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("123456789");
        testUser.setIsPrivacyEnabled(false);
        testUser.setPassword("encodedPassword");
        testUser.setRoles(new HashSet<>());

        testUserDTO = new UserDTO(
                1L,
                "test@example.com",
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

    // ==================== LIST USERS TESTS ====================

    @Test
    void shouldShowUsersList() throws Exception {
        // Given - controller uses userService.getAllUsers(pageable), NOT userRepository
        Page<UserDTO> userPage = new PageImpl<>(Collections.singletonList(testUserDTO));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users-list"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    void shouldShowUsersListWithPagination() throws Exception {
        // Given
        Page<UserDTO> userPage = new PageImpl<>(Collections.singletonList(testUserDTO));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/admin/users")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users-list"))
                .andExpect(model().attributeExists("pageSize"));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    // ==================== CREATE USER FORM TESTS ====================

    @Test
    void shouldShowCreateUserForm() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("isEdit", false));
    }

    // ==================== EDIT USER FORM TESTS ====================

    @Test
    void shouldShowEditUserForm() throws Exception {
        // Given - controller uses userService.getUserById(), NOT userRepository
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUserDTO));

        // When & Then
        mockMvc.perform(get("/admin/users/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("userId", 1L));

        verify(userService).getUserById(1L);
    }

    @Test
    void shouldRedirectWhenEditingNonExistentUser() throws Exception {
        // Given - controller redirects when user not found
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/admin/users/edit/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== CREATE USER TESTS ====================

    @Test
    void shouldCreateNewUser() throws Exception {
        // Given - controller uses userService.registerUser()
        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("email", "newuser@example.com")
                        .param("password", "password123")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("phone", "123456789")
                        .param("isPrivacyEnabled", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    void shouldReturnErrorWhenCreatingUserWithValidationErrors() throws Exception {
        // When & Then - missing required fields triggers validation error
        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("email", "")  // empty email - validation error
                        .param("firstName", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attribute("isEdit", false));

        verify(userService, never()).registerUser(any());
    }

    @Test
    void shouldReturnErrorWhenServiceThrowsException() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("email", "existing@example.com")
                        .param("password", "password123")
                        .param("firstName", "Test")
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeExists("error"));
    }

    // ==================== UPDATE USER TESTS ====================
    // NOTE: Controller endpoint is /admin/users/users/update/{id} (has extra /users in path!)

    @Test
    void shouldUpdateUser() throws Exception {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then - NOTE: endpoint path has /users/update not just /update
        mockMvc.perform(post("/admin/users/users/update/1")
                        .with(csrf())
                        .param("firstName", "Updated")
                        .param("lastName", "Name")
                        .param("phone", "999888777")
                        .param("isPrivacyEnabled", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserWithAvatar() throws Exception {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(fileStorageService.storeFile(any())).thenReturn("avatar.jpg");

        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/admin/users/users/update/1")
                        .file(avatarFile)
                        .with(csrf())
                        .param("firstName", "Updated")
                        .param("lastName", "Name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(fileStorageService).storeFile(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldReturnErrorWhenUpdatingNonExistentUser() throws Exception {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/admin/users/users/update/999")
                        .with(csrf())
                        .param("firstName", "Test")
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeExists("error"));

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    void shouldDeleteUser() throws Exception {
        // Given - controller uses userDao.executeUpdate(), NOT userRepository.delete()
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userDao).executeUpdate(anyString(), anyLong());

        // When & Then
        mockMvc.perform(get("/admin/users/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"));

        // Verify JDBC deletions were called (tickets, roles, user)
        verify(userDao, times(3)).executeUpdate(anyString(), eq(1L));
    }

    @Test
    void shouldDeleteUserWithAvatar() throws Exception {
        // Given
        testUser.setAvatarFilename("avatar.jpg");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(fileStorageService).deleteFile(anyString());
        doNothing().when(userDao).executeUpdate(anyString(), anyLong());

        // When & Then
        mockMvc.perform(get("/admin/users/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"));

        verify(fileStorageService).deleteFile("avatar.jpg");
    }

    @Test
    void shouldHandleErrorWhenDeletingUser() throws Exception {
        // Given - exception during deletion
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doThrow(new RuntimeException("Database error")).when(userDao).executeUpdate(anyString(), anyLong());

        // When & Then
        mockMvc.perform(get("/admin/users/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== AVATAR DELETE TESTS ====================

    @Test
    void shouldDeleteUserAvatar() throws Exception {
        // Given
        testUser.setAvatarFilename("avatar.jpg");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(fileStorageService).deleteFile(anyString());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/admin/users/1/avatar/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/edit/1"))
                .andExpect(flash().attributeExists("success"));

        verify(fileStorageService).deleteFile("avatar.jpg");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldHandleNoAvatarToDelete() throws Exception {
        // Given - user has no avatar
        testUser.setAvatarFilename(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/admin/users/1/avatar/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/edit/1"))
                .andExpect(flash().attributeExists("info"));

        verify(fileStorageService, never()).deleteFile(any());
    }

    // ==================== CSV IMPORT/EXPORT TESTS ====================

    @Test
    void shouldShowImportCsvPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/users/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/import-csv"));
    }

    @Test
    void shouldImportUsersFromCsv() throws Exception {
        // Given
        String csvContent = "email,firstName,lastName,phone,isPrivacyEnabled\n" +
                "test1@example.com,Test1,User1,123,false\n" +
                "test2@example.com,Test2,User2,456,true";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // Controller uses userDao.insertUser() for each user
        when(userDao.insertUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);  // Simulate ID assignment
            return 1;
        });
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(multipart("/admin/users/import")
                        .file(csvFile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"));

        // Verify insertUser was called for each CSV row
        verify(userDao, times(2)).insertUser(any(User.class));
    }

    @Test
    void shouldReturnErrorWhenImportingEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/admin/users/import")
                        .file(emptyFile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/import"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void shouldExportUsersToCsv() throws Exception {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));

        // When & Then
        mockMvc.perform(get("/admin/users/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"users_export.csv\""));

        verify(userRepository).findAll();
    }
}