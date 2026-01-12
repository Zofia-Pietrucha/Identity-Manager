package com.example.identitymanager.service;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserRegistrationDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.exception.DuplicateResourceException;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.RoleRepository;
import com.example.identitymanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDTO registrationDTO;
    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("Test");
        registrationDTO.setLastName("User");
        registrationDTO.setPhone("123456789");
        registrationDTO.setIsPrivacyEnabled(false);

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("123456789");
        testUser.setIsPrivacyEnabled(false);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.getRoles().add(userRole);
    }

    // ==================== REGISTER USER TESTS ====================

    @Test
    void shouldRegisterNewUser() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.registerUser(registrationDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getRoles()).contains("USER");

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(registrationDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldHashPasswordWhenRegistering() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        userService.registerUser(registrationDTO);

        // Then
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("$2a$10$hashedPassword")
        ));
    }

    @Test
    void shouldAssignUserRoleByDefault() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.registerUser(registrationDTO);

        // Then
        verify(roleRepository).findByName(Role.RoleName.USER);
        verify(userRepository).save(argThat(user ->
                user.getRoles().stream()
                        .anyMatch(role -> role.getName() == Role.RoleName.USER)
        ));
    }

    @Test
    void shouldRegisterUserWithoutRoleWhenRoleNotFound() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        // When
        UserDTO result = userService.registerUser(registrationDTO);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    // ==================== GET ALL USERS TESTS ====================

    @Test
    void shouldGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    // ==================== GET ALL USERS WITH PAGINATION TESTS ====================

    @Test
    void shouldGetAllUsersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenNoUsersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(userRepository).findAll(pageable);
    }

    // ==================== SEARCH USERS TESTS ====================

    @Test
    void shouldSearchUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);
        when(userRepository.searchUsers("test", pageable)).thenReturn(userPage);

        // When
        Page<UserDTO> result = userService.searchUsers("test", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        verify(userRepository).searchUsers("test", pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenSearchNoResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userRepository.searchUsers("nonexistent", pageable)).thenReturn(emptyPage);

        // When
        Page<UserDTO> result = userService.searchUsers("nonexistent", pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        verify(userRepository).searchUsers("nonexistent", pageable);
    }

    // ==================== GET USERS BY ROLE TESTS ====================

    @Test
    void shouldGetUsersByRole() {
        // Given
        when(userRepository.findUsersByRoleName(Role.RoleName.USER))
                .thenReturn(Arrays.asList(testUser));

        // When
        List<UserDTO> result = userService.getUsersByRole("USER");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findUsersByRoleName(Role.RoleName.USER);
    }

    @Test
    void shouldGetUsersByRoleCaseInsensitive() {
        // Given
        when(userRepository.findUsersByRoleName(Role.RoleName.ADMIN))
                .thenReturn(Collections.emptyList());

        // When
        List<UserDTO> result = userService.getUsersByRole("admin");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findUsersByRoleName(Role.RoleName.ADMIN);
    }

    @Test
    void shouldThrowExceptionForInvalidRole() {
        // When & Then
        assertThatThrownBy(() -> userService.getUsersByRole("INVALID_ROLE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role name");
    }

    // ==================== COUNT USERS WITH PRIVACY ENABLED TESTS ====================

    @Test
    void shouldCountUsersWithPrivacyEnabled() {
        // Given
        when(userRepository.countUsersWithPrivacyEnabled()).thenReturn(5L);

        // When
        long result = userService.countUsersWithPrivacyEnabled();

        // Then
        assertThat(result).isEqualTo(5L);
        verify(userRepository).countUsersWithPrivacyEnabled();
    }

    @Test
    void shouldReturnZeroWhenNoUsersWithPrivacyEnabled() {
        // Given
        when(userRepository.countUsersWithPrivacyEnabled()).thenReturn(0L);

        // When
        long result = userService.countUsersWithPrivacyEnabled();

        // Then
        assertThat(result).isEqualTo(0L);
        verify(userRepository).countUsersWithPrivacyEnabled();
    }

    // ==================== SEARCH USERS BY NAME TESTS ====================

    @Test
    void shouldSearchUsersByName() {
        // Given
        when(userRepository.searchUsersByName("Test"))
                .thenReturn(Arrays.asList(testUser));

        // When
        List<UserDTO> result = userService.searchUsersByName("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Test");
        verify(userRepository).searchUsersByName("Test");
    }

    @Test
    void shouldReturnEmptyListWhenSearchByNameNoResults() {
        // Given
        when(userRepository.searchUsersByName("NonExistent"))
                .thenReturn(Collections.emptyList());

        // When
        List<UserDTO> result = userService.searchUsersByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).searchUsersByName("NonExistent");
    }

    // ==================== GET USER BY ID TESTS ====================

    @Test
    void shouldGetUserById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDTO> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenUserIdNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<UserDTO> result = userService.getUserById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(999L);
    }

    // ==================== GET USER BY EMAIL TESTS ====================

    @Test
    void shouldGetUserByEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDTO> result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // Given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When
        Optional<UserDTO> result = userService.getUserByEmail("notfound@example.com");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail("notfound@example.com");
    }

    // ==================== UPDATE USER PROFILE TESTS ====================

    @Test
    void shouldUpdateUserProfile() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated", "Name", "999888777");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDTO result = userService.updateUserProfile("test@example.com", updateDTO);

        // Then
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Name");
        assertThat(result.getPhone()).isEqualTo("999888777");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingProfileOfNonExistentUser() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated", "Name", "999888777");
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserProfile("notfound@example.com", updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("notfound@example.com");
    }

    // ==================== UPDATE USER BY ID TESTS ====================

    @Test
    void shouldUpdateUserById() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated", "Name", "999888777");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Then
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Name");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUserById() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO("Updated", "Name", "999888777");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    void shouldDeleteUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).delete(any(User.class));
    }

    // ==================== UPDATE PRIVACY SETTINGS TESTS ====================

    @Test
    void shouldUpdatePrivacySettings() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDTO result = userService.updatePrivacySettings("test@example.com", true);

        // Then
        assertThat(result.getIsPrivacyEnabled()).isTrue();
        verify(userRepository).save(argThat(user -> user.getIsPrivacyEnabled()));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingPrivacyForNonExistentUser() {
        // Given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updatePrivacySettings("notfound@example.com", true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== UPDATE USER AVATAR TESTS ====================

    @Test
    void shouldUpdateUserAvatar() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateUserAvatar("test@example.com", "avatar.jpg");

        // Then
        verify(userRepository).save(argThat(user ->
                "avatar.jpg".equals(user.getAvatarFilename())
        ));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingAvatarForNonExistentUser() {
        // Given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserAvatar("notfound@example.com", "avatar.jpg"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== CREATE USER WITH ENCODED PASSWORD TESTS ====================

    @Test
    void shouldCreateUserWithEncodedPassword() {
        // Given
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.createUserWithEncodedPassword(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(testUser);
        verify(passwordEncoder, never()).encode(anyString()); // Password should NOT be encoded
    }

    // ==================== DTO CONVERSION TESTS ====================

    @Test
    void shouldConvertUserToDTO() {
        // Given
        testUser.setAvatarFilename("avatar.jpg");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDTO> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        UserDTO dto = result.get();
        assertThat(dto.getId()).isEqualTo(testUser.getId());
        assertThat(dto.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(dto.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(dto.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(dto.getPhone()).isEqualTo(testUser.getPhone());
        assertThat(dto.getIsPrivacyEnabled()).isEqualTo(testUser.getIsPrivacyEnabled());
        assertThat(dto.getRoles()).contains("USER");
        assertThat(dto.getAvatarFilename()).isEqualTo("avatar.jpg");
        assertThat(dto.getAvatarUrl()).isNull(); // URL is set by controller, not service
    }
}