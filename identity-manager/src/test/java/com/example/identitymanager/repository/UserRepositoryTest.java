package com.example.identitymanager.repository;

import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create and persist role
        userRole = new Role();
        userRole.setName(Role.RoleName.USER);
        entityManager.persist(userRole);

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("123456789");
        testUser.setIsPrivacyEnabled(false);
        testUser.getRoles().add(userRole);

        entityManager.persistAndFlush(testUser);
    }

    @Test
    void shouldSaveUser() {
        // Given
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setIsPrivacyEnabled(true);

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getIsPrivacyEnabled()).isTrue();
    }

    @Test
    void shouldFindUserByEmail() {
        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindAllUsers() {
        // Given - one user already created in setUp
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setIsPrivacyEnabled(false);
        entityManager.persistAndFlush(anotherUser);

        // When
        List<User> users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("test@example.com", "another@example.com");
    }

    @Test
    void shouldDeleteUser() {
        // When
        userRepository.delete(testUser);
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateUser() {
        // Given
        testUser.setFirstName("Updated");
        testUser.setIsPrivacyEnabled(true);

        // When
        User updated = userRepository.save(testUser);

        // Then
        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getIsPrivacyEnabled()).isTrue();
        assertThat(updated.getEmail()).isEqualTo("test@example.com"); // Email unchanged
    }

    @Test
    void shouldPersistUserWithRoles() {
        // Given
        Role adminRole = new Role();
        adminRole.setName(Role.RoleName.ADMIN);
        entityManager.persist(adminRole);

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setIsPrivacyEnabled(false);
        adminUser.getRoles().add(userRole);
        adminUser.getRoles().add(adminRole);

        // When
        User saved = userRepository.save(adminUser);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context

        User found = userRepository.findById(saved.getId()).orElseThrow();

        // Then
        assertThat(found.getRoles()).hasSize(2);
        assertThat(found.getRoles()).extracting(Role::getName)
                .containsExactlyInAnyOrder(Role.RoleName.USER, Role.RoleName.ADMIN);
    }
}