package com.example.identitymanager.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ModelTest {

    // ==================== USER TESTS ====================

    @Test
    void shouldCreateUserWithNoArgsConstructor() {
        // When
        User user = new User();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getRoles()).isNotNull().isEmpty();
        assertThat(user.getTickets()).isNotNull().isEmpty();
    }

    @Test
    void shouldCreateUserWithArgsConstructor() {
        // When
        User user = new User("test@example.com", "password123", "John", "Doe");

        // Then
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getIsPrivacyEnabled()).isFalse();
    }

    @Test
    void shouldSetAndGetAllUserFields() {
        // Given
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        Set<Role> roles = new HashSet<>();
        Set<SupportTicket> tickets = new HashSet<>();

        // When
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("123456789");
        user.setIsPrivacyEnabled(true);
        user.setAvatarFilename("avatar.jpg");
        user.setRoles(roles);
        user.setTickets(tickets);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getPhone()).isEqualTo("123456789");
        assertThat(user.getIsPrivacyEnabled()).isTrue();
        assertThat(user.getAvatarFilename()).isEqualTo("avatar.jpg");
        assertThat(user.getRoles()).isSameAs(roles);
        assertThat(user.getTickets()).isSameAs(tickets);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetTimestampsOnCreate() {
        // Given
        User user = new User();

        // When - simulate @PrePersist
        user.onCreate();

        // Then
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        // Given
        User user = new User();
        user.onCreate();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();

        // Wait 1ms to ensure different timestamp
        Thread.sleep(10);

        // When - simulate @PreUpdate
        user.onUpdate();

        // Then
        assertThat(user.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(user.getCreatedAt()).isBefore(user.getUpdatedAt());
    }

    @Test
    void shouldHaveCorrectUserEqualsAndHashCode() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("test@example.com");

        User user2 = new User();
        user2.setId(1L);
        user2.setEmail("test@example.com");

        User user3 = new User();
        user3.setId(2L);
        user3.setEmail("other@example.com");

        // Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1).isNotEqualTo(user3);
    }

    @Test
    void shouldHaveCorrectUserToString() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("User");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("email=test@example.com");
        assertThat(toString).contains("firstName=John");
    }

    @Test
    void shouldAddRoleToUser() {
        // Given
        User user = new User();
        Role role = new Role();
        role.setName(Role.RoleName.USER);

        // When
        user.getRoles().add(role);

        // Then
        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles()).contains(role);
    }

    @Test
    void shouldAddTicketToUser() {
        // Given
        User user = new User();
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject("Test");

        // When
        user.getTickets().add(ticket);

        // Then
        assertThat(user.getTickets()).hasSize(1);
        assertThat(user.getTickets()).contains(ticket);
    }

    // ==================== ROLE TESTS ====================

    @Test
    void shouldCreateRoleWithNoArgsConstructor() {
        // When
        Role role = new Role();

        // Then
        assertThat(role).isNotNull();
        assertThat(role.getId()).isNull();
        assertThat(role.getName()).isNull();
    }

    @Test
    void shouldCreateRoleWithAllArgsConstructor() {
        // When
        Role role = new Role(1L, Role.RoleName.ADMIN);

        // Then
        assertThat(role.getId()).isEqualTo(1L);
        assertThat(role.getName()).isEqualTo(Role.RoleName.ADMIN);
    }

    @Test
    void shouldSetAndGetRoleFields() {
        // Given
        Role role = new Role();

        // When
        role.setId(1L);
        role.setName(Role.RoleName.USER);

        // Then
        assertThat(role.getId()).isEqualTo(1L);
        assertThat(role.getName()).isEqualTo(Role.RoleName.USER);
    }

    @Test
    void shouldHaveAllRoleNames() {
        // Then
        assertThat(Role.RoleName.values()).containsExactlyInAnyOrder(
                Role.RoleName.USER,
                Role.RoleName.ADMIN
        );
    }

    @Test
    void shouldHaveCorrectRoleEqualsAndHashCode() {
        // Given
        Role role1 = new Role(1L, Role.RoleName.USER);
        Role role2 = new Role(1L, Role.RoleName.USER);
        Role role3 = new Role(2L, Role.RoleName.ADMIN);

        // Then
        assertThat(role1).isEqualTo(role2);
        assertThat(role1.hashCode()).isEqualTo(role2.hashCode());
        assertThat(role1).isNotEqualTo(role3);
    }

    @Test
    void shouldHaveCorrectRoleToString() {
        // Given
        Role role = new Role(1L, Role.RoleName.ADMIN);

        // When
        String toString = role.toString();

        // Then
        assertThat(toString).contains("Role");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("ADMIN");
    }

    @Test
    void shouldConvertRoleNameFromString() {
        // When
        Role.RoleName user = Role.RoleName.valueOf("USER");
        Role.RoleName admin = Role.RoleName.valueOf("ADMIN");

        // Then
        assertThat(user).isEqualTo(Role.RoleName.USER);
        assertThat(admin).isEqualTo(Role.RoleName.ADMIN);
    }

    // ==================== SUPPORT TICKET TESTS ====================

    @Test
    void shouldCreateSupportTicketWithNoArgsConstructor() {
        // When
        SupportTicket ticket = new SupportTicket();

        // Then
        assertThat(ticket).isNotNull();
        assertThat(ticket.getId()).isNull();
        assertThat(ticket.getStatus()).isEqualTo(SupportTicket.TicketStatus.OPEN);
    }

    @Test
    void shouldCreateSupportTicketWithAllArgsConstructor() {
        // Given
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        // When
        SupportTicket ticket = new SupportTicket(
                1L, "Subject", "Description",
                SupportTicket.TicketStatus.IN_PROGRESS, user, now
        );

        // Then
        assertThat(ticket.getId()).isEqualTo(1L);
        assertThat(ticket.getSubject()).isEqualTo("Subject");
        assertThat(ticket.getDescription()).isEqualTo("Description");
        assertThat(ticket.getStatus()).isEqualTo(SupportTicket.TicketStatus.IN_PROGRESS);
        assertThat(ticket.getUser()).isSameAs(user);
        assertThat(ticket.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetAllSupportTicketFields() {
        // Given
        SupportTicket ticket = new SupportTicket();
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        // When
        ticket.setId(1L);
        ticket.setSubject("Test Subject");
        ticket.setDescription("Test Description");
        ticket.setStatus(SupportTicket.TicketStatus.RESOLVED);
        ticket.setUser(user);
        ticket.setCreatedAt(now);

        // Then
        assertThat(ticket.getId()).isEqualTo(1L);
        assertThat(ticket.getSubject()).isEqualTo("Test Subject");
        assertThat(ticket.getDescription()).isEqualTo("Test Description");
        assertThat(ticket.getStatus()).isEqualTo(SupportTicket.TicketStatus.RESOLVED);
        assertThat(ticket.getUser()).isSameAs(user);
        assertThat(ticket.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetTimestampOnTicketCreate() {
        // Given
        SupportTicket ticket = new SupportTicket();

        // When - simulate @PrePersist
        ticket.onCreate();

        // Then
        assertThat(ticket.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldHaveAllTicketStatuses() {
        // Then
        assertThat(SupportTicket.TicketStatus.values()).containsExactlyInAnyOrder(
                SupportTicket.TicketStatus.OPEN,
                SupportTicket.TicketStatus.IN_PROGRESS,
                SupportTicket.TicketStatus.RESOLVED,
                SupportTicket.TicketStatus.CLOSED
        );
    }

    @Test
    void shouldHaveCorrectSupportTicketEqualsAndHashCode() {
        // Given
        SupportTicket ticket1 = new SupportTicket();
        ticket1.setId(1L);
        ticket1.setSubject("Test");

        SupportTicket ticket2 = new SupportTicket();
        ticket2.setId(1L);
        ticket2.setSubject("Test");

        SupportTicket ticket3 = new SupportTicket();
        ticket3.setId(2L);
        ticket3.setSubject("Other");

        // Then
        assertThat(ticket1).isEqualTo(ticket2);
        assertThat(ticket1.hashCode()).isEqualTo(ticket2.hashCode());
        assertThat(ticket1).isNotEqualTo(ticket3);
    }

    @Test
    void shouldHaveCorrectSupportTicketToString() {
        // Given
        SupportTicket ticket = new SupportTicket();
        ticket.setId(1L);
        ticket.setSubject("Test Subject");
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);

        // When
        String toString = ticket.toString();

        // Then
        assertThat(toString).contains("SupportTicket");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("subject=Test Subject");
        assertThat(toString).contains("OPEN");
    }

    @Test
    void shouldConvertTicketStatusFromString() {
        // When
        SupportTicket.TicketStatus open = SupportTicket.TicketStatus.valueOf("OPEN");
        SupportTicket.TicketStatus inProgress = SupportTicket.TicketStatus.valueOf("IN_PROGRESS");
        SupportTicket.TicketStatus resolved = SupportTicket.TicketStatus.valueOf("RESOLVED");
        SupportTicket.TicketStatus closed = SupportTicket.TicketStatus.valueOf("CLOSED");

        // Then
        assertThat(open).isEqualTo(SupportTicket.TicketStatus.OPEN);
        assertThat(inProgress).isEqualTo(SupportTicket.TicketStatus.IN_PROGRESS);
        assertThat(resolved).isEqualTo(SupportTicket.TicketStatus.RESOLVED);
        assertThat(closed).isEqualTo(SupportTicket.TicketStatus.CLOSED);
    }

    @Test
    void shouldTransitionTicketStatusCorrectly() {
        // Given
        SupportTicket ticket = new SupportTicket();
        assertThat(ticket.getStatus()).isEqualTo(SupportTicket.TicketStatus.OPEN);

        // When & Then - transition through all statuses
        ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicket.TicketStatus.IN_PROGRESS);

        ticket.setStatus(SupportTicket.TicketStatus.RESOLVED);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicket.TicketStatus.RESOLVED);

        ticket.setStatus(SupportTicket.TicketStatus.CLOSED);
        assertThat(ticket.getStatus()).isEqualTo(SupportTicket.TicketStatus.CLOSED);
    }

    // ==================== EDGE CASES ====================

    @Test
    void shouldHandleNullValues() {
        // Given
        User user = new User();

        // When
        user.setPhone(null);
        user.setAvatarFilename(null);

        // Then
        assertThat(user.getPhone()).isNull();
        assertThat(user.getAvatarFilename()).isNull();
    }

    @Test
    void shouldHandleEmptyCollections() {
        // Given
        User user = new User();

        // Then
        assertThat(user.getRoles()).isEmpty();
        assertThat(user.getTickets()).isEmpty();
    }

    @Test
    void userShouldNotEqualNull() {
        // Given
        User user = new User();
        user.setId(1L);

        // Then
        assertThat(user).isNotEqualTo(null);
    }

    @Test
    void userShouldNotEqualDifferentClass() {
        // Given
        User user = new User();
        user.setId(1L);
        String notAUser = "not a user";

        // Then
        assertThat(user).isNotEqualTo(notAUser);
    }

    @Test
    void userShouldEqualItself() {
        // Given
        User user = new User();
        user.setId(1L);

        // Then
        assertThat(user).isEqualTo(user);
    }
}