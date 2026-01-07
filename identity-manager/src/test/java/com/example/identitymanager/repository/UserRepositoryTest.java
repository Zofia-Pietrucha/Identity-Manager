package com.example.identitymanager.repository;

import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    private Role adminRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create and persist roles
        userRole = new Role();
        userRole.setName(Role.RoleName.USER);
        entityManager.persist(userRole);

        adminRole = new Role();
        adminRole.setName(Role.RoleName.ADMIN);
        entityManager.persist(adminRole);

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

    // ==================== BASIC CRUD TESTS ====================

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

    // ==================== PAGINATION TESTS ====================

    @Test
    void shouldFindAllWithPagination() {
        // Given - create more users
        for (int i = 0; i < 15; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password");
            user.setFirstName("User" + i);
            user.setLastName("Test");
            user.setIsPrivacyEnabled(false);
            entityManager.persist(user);
        }
        entityManager.flush();

        // When
        Pageable pageable = PageRequest.of(0, 5);
        Page<User> page = userRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(16); // 15 + 1 from setUp
        assertThat(page.getTotalPages()).isEqualTo(4);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    void shouldGetSecondPage() {
        // Given - create more users
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password");
            user.setFirstName("User" + i);
            user.setLastName("Test");
            user.setIsPrivacyEnabled(false);
            entityManager.persist(user);
        }
        entityManager.flush();

        // When
        Pageable pageable = PageRequest.of(1, 5);
        Page<User> page = userRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    // ==================== CUSTOM QUERY TESTS ====================

    @Test
    void shouldFindUsersByRoleName() {
        // Given - create admin user
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setIsPrivacyEnabled(false);
        adminUser.getRoles().add(adminRole);
        entityManager.persistAndFlush(adminUser);

        // When
        List<User> admins = userRepository.findUsersByRoleName(Role.RoleName.ADMIN);
        List<User> users = userRepository.findUsersByRoleName(Role.RoleName.USER);

        // Then
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getEmail()).isEqualTo("admin@example.com");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersWithRole() {
        // Given - remove user role from testUser
        testUser.getRoles().clear();
        entityManager.persistAndFlush(testUser);

        // When
        List<User> users = userRepository.findUsersByRoleName(Role.RoleName.USER);

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    void shouldCountUsersWithPrivacyEnabled() {
        // Given
        testUser.setIsPrivacyEnabled(true);
        entityManager.persist(testUser);

        User publicUser = new User();
        publicUser.setEmail("public@example.com");
        publicUser.setPassword("password");
        publicUser.setFirstName("Public");
        publicUser.setLastName("User");
        publicUser.setIsPrivacyEnabled(false);
        entityManager.persist(publicUser);

        User privateUser = new User();
        privateUser.setEmail("private@example.com");
        privateUser.setPassword("password");
        privateUser.setFirstName("Private");
        privateUser.setLastName("User");
        privateUser.setIsPrivacyEnabled(true);
        entityManager.persistAndFlush(privateUser);

        // When
        long count = userRepository.countUsersWithPrivacyEnabled();

        // Then
        assertThat(count).isEqualTo(2); // testUser + privateUser
    }

    @Test
    void shouldReturnZeroWhenNoUsersWithPrivacyEnabled() {
        // Given - testUser already has privacy disabled

        // When
        long count = userRepository.countUsersWithPrivacyEnabled();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldSearchUsersByName() {
        // Given
        User john = new User();
        john.setEmail("john@example.com");
        john.setPassword("password");
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setIsPrivacyEnabled(false);
        entityManager.persist(john);

        User jane = new User();
        jane.setEmail("jane@example.com");
        jane.setPassword("password");
        jane.setFirstName("Jane");
        jane.setLastName("Smith");
        jane.setIsPrivacyEnabled(false);
        entityManager.persistAndFlush(jane);

        // When - search by first name "Doe" (unique)
        List<User> does = userRepository.searchUsersByName("Doe");

        // Then
        assertThat(does).hasSize(1);
        assertThat(does.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldSearchUsersByLastName() {
        // Given
        User john = new User();
        john.setEmail("john@example.com");
        john.setPassword("password");
        john.setFirstName("John");
        john.setLastName("Smith");
        john.setIsPrivacyEnabled(false);
        entityManager.persistAndFlush(john);

        // When - search by last name
        List<User> smiths = userRepository.searchUsersByName("Smith");

        // Then
        assertThat(smiths).hasSize(1);
        assertThat(smiths.get(0).getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldSearchUsersByNameCaseInsensitive() {
        // When - search with different case
        List<User> users = userRepository.searchUsersByName("TEST");

        // Then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("Test");
    }

    @Test
    void shouldSearchUsersByPartialName() {
        // When - search with partial name
        List<User> users = userRepository.searchUsersByName("est");

        // Then
        assertThat(users).hasSize(1); // matches "Test"
    }

    // ==================== SEARCH WITH PAGINATION TESTS ====================

    @Test
    void shouldSearchUsersWithPagination() {
        // Given
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("search" + i + "@example.com");
            user.setPassword("password");
            user.setFirstName("Search" + i);
            user.setLastName("User");
            user.setIsPrivacyEnabled(false);
            entityManager.persist(user);
        }
        entityManager.flush();

        // When
        Pageable pageable = PageRequest.of(0, 5);
        Page<User> page = userRepository.searchUsers("search", pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(10);
    }

    @Test
    void shouldSearchUsersByEmailWithPagination() {
        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = userRepository.searchUsers("test@example.com", pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyPageWhenSearchNoResults() {
        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = userRepository.searchUsers("nonexistent", pageable);

        // Then
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    // ==================== AVATAR TESTS ====================

    @Test
    void shouldSaveUserWithAvatar() {
        // Given
        testUser.setAvatarFilename("avatar.jpg");

        // When
        User saved = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(saved.getId()).orElseThrow();

        // Then
        assertThat(found.getAvatarFilename()).isEqualTo("avatar.jpg");
    }

    @Test
    void shouldUpdateUserAvatar() {
        // Given
        testUser.setAvatarFilename("old-avatar.jpg");
        entityManager.persistAndFlush(testUser);

        // When
        testUser.setAvatarFilename("new-avatar.jpg");
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(testUser.getId()).orElseThrow();

        // Then
        assertThat(found.getAvatarFilename()).isEqualTo("new-avatar.jpg");
    }

    @Test
    void shouldRemoveUserAvatar() {
        // Given
        testUser.setAvatarFilename("avatar.jpg");
        entityManager.persistAndFlush(testUser);

        // When
        testUser.setAvatarFilename(null);
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(testUser.getId()).orElseThrow();

        // Then
        assertThat(found.getAvatarFilename()).isNull();
    }
}