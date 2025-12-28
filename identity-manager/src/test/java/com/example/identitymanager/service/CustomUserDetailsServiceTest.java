package com.example.identitymanager.service;

import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);
        testUser.getRoles().add(userRole);
    }

    @Test
    void shouldLoadUserByUsername() {
        // Given
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("notfound@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("notfound@example.com");
    }

    @Test
    void shouldConvertRolesToAuthorities() {
        // Given - user with ADMIN and USER roles
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(Role.RoleName.ADMIN);
        testUser.getRoles().add(adminRole);

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");

        // Then
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}