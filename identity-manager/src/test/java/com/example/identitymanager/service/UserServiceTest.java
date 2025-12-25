package com.example.identitymanager.service;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserRegistrationDTO;
import com.example.identitymanager.exception.DuplicateResourceException;
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
    void shouldConvertUserToDTO() {
        // Given
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
}