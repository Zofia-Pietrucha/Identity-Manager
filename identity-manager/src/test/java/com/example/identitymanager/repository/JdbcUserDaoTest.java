package com.example.identitymanager.repository;

import com.example.identitymanager.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcUserDao.class)
@Sql(scripts = "/test-schema.sql")
class JdbcUserDaoTest {

    @Autowired
    private JdbcUserDao jdbcUserDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== SELECT OPERATIONS TESTS ====================

    @Test
    void shouldFindAllUsers() {
        // Given - insert test data
        insertTestUser("user1@test.com", "User", "One", "111");
        insertTestUser("user2@test.com", "User", "Two", "222");

        // When
        List<User> users = jdbcUserDao.findAllUsers();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@test.com", "user2@test.com");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        // When
        List<User> users = jdbcUserDao.findAllUsers();

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    void shouldFindUserById() {
        // Given
        Long userId = insertTestUser("test@test.com", "Test", "User", "123");

        // When
        Optional<User> user = jdbcUserDao.findUserById(userId);

        // Then
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("test@test.com");
        assertThat(user.get().getFirstName()).isEqualTo("Test");
        assertThat(user.get().getLastName()).isEqualTo("User");
    }

    @Test
    void shouldReturnEmptyWhenUserIdNotFound() {
        // When
        Optional<User> user = jdbcUserDao.findUserById(999L);

        // Then
        assertThat(user).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        insertTestUser("find@test.com", "Find", "Me", "123");

        // When
        Optional<User> user = jdbcUserDao.findUserByEmail("find@test.com");

        // Then
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("find@test.com");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<User> user = jdbcUserDao.findUserByEmail("notfound@test.com");

        // Then
        assertThat(user).isEmpty();
    }

    @Test
    void shouldCountUsers() {
        // Given
        insertTestUser("count1@test.com", "Count", "One", "111");
        insertTestUser("count2@test.com", "Count", "Two", "222");
        insertTestUser("count3@test.com", "Count", "Three", "333");

        // When
        int count = jdbcUserDao.countUsers();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroWhenNoUsers() {
        // When
        int count = jdbcUserDao.countUsers();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldFindUsersByPrivacyEnabled() {
        // Given
        Long user1 = insertTestUser("private@test.com", "Private", "User", "111");
        Long user2 = insertTestUser("public@test.com", "Public", "User", "222");

        // Set privacy for first user
        jdbcTemplate.update("UPDATE users SET is_privacy_enabled = true WHERE id = ?", user1);

        // When
        List<User> privateUsers = jdbcUserDao.findUsersByPrivacyEnabled(true);
        List<User> publicUsers = jdbcUserDao.findUsersByPrivacyEnabled(false);

        // Then
        assertThat(privateUsers).hasSize(1);
        assertThat(privateUsers.get(0).getEmail()).isEqualTo("private@test.com");
        assertThat(privateUsers.get(0).getIsPrivacyEnabled()).isTrue();

        assertThat(publicUsers).hasSize(1);
        assertThat(publicUsers.get(0).getEmail()).isEqualTo("public@test.com");
        assertThat(publicUsers.get(0).getIsPrivacyEnabled()).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersWithPrivacyEnabled() {
        // Given
        insertTestUser("user@test.com", "Test", "User", "123");

        // When
        List<User> privateUsers = jdbcUserDao.findUsersByPrivacyEnabled(true);

        // Then
        assertThat(privateUsers).isEmpty();
    }

    // ==================== INSERT OPERATION TESTS ====================

    @Test
    void shouldInsertUser() {
        // Given
        User user = new User();
        user.setEmail("new@test.com");
        user.setPassword("password123");
        user.setFirstName("New");
        user.setLastName("User");
        user.setPhone("555666777");
        user.setIsPrivacyEnabled(false);

        // When
        int rowsAffected = jdbcUserDao.insertUser(user);

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        assertThat(user.getId()).isNotNull();

        // Verify user was inserted
        Optional<User> found = jdbcUserDao.findUserByEmail("new@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("New");
    }

    @Test
    void shouldInsertUserWithPrivacyEnabled() {
        // Given
        User user = new User();
        user.setEmail("private@test.com");
        user.setPassword("password123");
        user.setFirstName("Private");
        user.setLastName("User");
        user.setPhone("123456789");
        user.setIsPrivacyEnabled(true);

        // When
        jdbcUserDao.insertUser(user);

        // Then
        Optional<User> found = jdbcUserDao.findUserByEmail("private@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getIsPrivacyEnabled()).isTrue();
    }

    @Test
    void shouldInsertUserWithNullPhone() {
        // Given
        User user = new User();
        user.setEmail("nophone@test.com");
        user.setPassword("password123");
        user.setFirstName("No");
        user.setLastName("Phone");
        user.setPhone(null);
        user.setIsPrivacyEnabled(false);

        // When
        int rowsAffected = jdbcUserDao.insertUser(user);

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        Optional<User> found = jdbcUserDao.findUserByEmail("nophone@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getPhone()).isNull();
    }

    @Test
    void shouldSetIdAfterInsert() {
        // Given
        User user = new User();
        user.setEmail("withid@test.com");
        user.setPassword("password123");
        user.setFirstName("With");
        user.setLastName("Id");
        user.setIsPrivacyEnabled(false);

        assertThat(user.getId()).isNull();

        // When
        jdbcUserDao.insertUser(user);

        // Then
        assertThat(user.getId()).isNotNull();
        assertThat(user.getId()).isGreaterThan(0);
    }

    // ==================== UPDATE OPERATION TESTS ====================

    @Test
    void shouldUpdateUser() {
        // Given
        Long userId = insertTestUser("update@test.com", "Old", "Name", "111");
        User user = jdbcUserDao.findUserById(userId).orElseThrow();

        user.setFirstName("New");
        user.setLastName("Updated");
        user.setPhone("999888777");
        user.setIsPrivacyEnabled(true);

        // When
        int rowsAffected = jdbcUserDao.updateUser(user);

        // Then
        assertThat(rowsAffected).isEqualTo(1);

        User updated = jdbcUserDao.findUserById(userId).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("New");
        assertThat(updated.getLastName()).isEqualTo("Updated");
        assertThat(updated.getPhone()).isEqualTo("999888777");
        assertThat(updated.getIsPrivacyEnabled()).isTrue();
    }

    @Test
    void shouldNotUpdateEmailOnUpdate() {
        // Given
        Long userId = insertTestUser("original@test.com", "Test", "User", "123");
        User user = jdbcUserDao.findUserById(userId).orElseThrow();

        // Email is not part of updateUser SQL
        user.setFirstName("Updated");

        // When
        jdbcUserDao.updateUser(user);

        // Then
        User updated = jdbcUserDao.findUserById(userId).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("original@test.com");
    }

    @Test
    void shouldReturnZeroWhenUpdatingNonExistentUser() {
        // Given
        User user = new User();
        user.setId(999L);
        user.setFirstName("Ghost");
        user.setLastName("User");
        user.setIsPrivacyEnabled(false);

        // When
        int rowsAffected = jdbcUserDao.updateUser(user);

        // Then
        assertThat(rowsAffected).isEqualTo(0);
    }

    // ==================== DELETE OPERATION TESTS ====================

    @Test
    void shouldDeleteUserById() {
        // Given
        Long userId = insertTestUser("delete@test.com", "Delete", "Me", "123");

        // When
        int rowsAffected = jdbcUserDao.deleteUserById(userId);

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        assertThat(jdbcUserDao.findUserById(userId)).isEmpty();
    }

    @Test
    void shouldReturnZeroWhenDeletingNonExistentUser() {
        // When
        int rowsAffected = jdbcUserDao.deleteUserById(999L);

        // Then
        assertThat(rowsAffected).isEqualTo(0);
    }

    @Test
    void shouldDeleteUserWithRoles() {
        // Given
        Long userId = insertTestUser("withroles@test.com", "With", "Roles", "123");

        // First insert a role
        jdbcTemplate.update("INSERT INTO roles (id, name) VALUES (1, 'USER')");

        // Add user_roles entry
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, 1)", userId);

        // When
        int rowsAffected = jdbcUserDao.deleteUserById(userId);

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        assertThat(jdbcUserDao.findUserById(userId)).isEmpty();

        // Verify user_roles was also deleted
        Integer roleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_roles WHERE user_id = ?",
                Integer.class, userId);
        assertThat(roleCount).isEqualTo(0);
    }

    // ==================== EXECUTE UPDATE TESTS ====================

    @Test
    void shouldExecuteCustomUpdate() {
        // Given
        Long userId = insertTestUser("custom@test.com", "Custom", "User", "123");

        // When
        jdbcUserDao.executeUpdate("UPDATE users SET first_name = ? WHERE id = ?", "Modified", userId);

        // Then
        User user = jdbcUserDao.findUserById(userId).orElseThrow();
        assertThat(user.getFirstName()).isEqualTo("Modified");
    }

    @Test
    void shouldExecuteCustomDelete() {
        // Given
        insertTestUser("todelete@test.com", "To", "Delete", "123");

        // When
        jdbcUserDao.executeUpdate("DELETE FROM users WHERE email = ?", "todelete@test.com");

        // Then
        assertThat(jdbcUserDao.findUserByEmail("todelete@test.com")).isEmpty();
    }

    // ==================== ROW MAPPER TESTS ====================

    @Test
    void shouldMapAllFieldsCorrectly() {
        // Given
        Long userId = insertTestUser("mapper@test.com", "Mapper", "Test", "123456789");
        jdbcTemplate.update("UPDATE users SET is_privacy_enabled = true WHERE id = ?", userId);

        // When
        User user = jdbcUserDao.findUserById(userId).orElseThrow();

        // Then
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo("mapper@test.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getFirstName()).isEqualTo("Mapper");
        assertThat(user.getLastName()).isEqualTo("Test");
        assertThat(user.getPhone()).isEqualTo("123456789");
        assertThat(user.getIsPrivacyEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getRoles()).isEmpty(); // JDBC doesn't load roles
    }

    // Helper method to insert test users
    private Long insertTestUser(String email, String firstName, String lastName, String phone) {
        jdbcTemplate.update(
                "INSERT INTO users (email, password, first_name, last_name, phone, is_privacy_enabled, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                email, "password123", firstName, lastName, phone, false
        );

        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                email
        );
    }
}