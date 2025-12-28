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
    void shouldReturnZeroWhenNoUsers() {
        // When
        int count = jdbcUserDao.countUsers();

        // Then
        assertThat(count).isEqualTo(0);
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