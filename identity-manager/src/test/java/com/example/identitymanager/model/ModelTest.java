package com.example.identitymanager.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelTest {

    @Test
    void shouldCreateUser() {
        // When
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        // Then
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldCreateRole() {
        // When
        Role role = new Role();
        role.setName(Role.RoleName.ADMIN);

        // Then
        assertThat(role.getName()).isEqualTo(Role.RoleName.ADMIN);
    }

    @Test
    void shouldCreateSupportTicket() {
        // When
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject("Test");
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);

        // Then
        assertThat(ticket.getSubject()).isEqualTo("Test");
        assertThat(ticket.getStatus()).isEqualTo(SupportTicket.TicketStatus.OPEN);
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
    void shouldHaveAllRoleNames() {
        // Then
        assertThat(Role.RoleName.values()).containsExactlyInAnyOrder(
                Role.RoleName.USER,
                Role.RoleName.ADMIN
        );
    }

    @Test
    void shouldSetTimestampsOnCreate() {
        // Given
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("pass");

        // When - simulate @PrePersist
        user.onCreate();

        // Then
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        // Given
        User user = new User();
        user.onCreate();
        var originalUpdatedAt = user.getUpdatedAt();

        // Wait 1ms to ensure different timestamp
        Thread.sleep(1);

        // When - simulate @PreUpdate
        user.onUpdate();

        // Then
        assertThat(user.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void shouldSetTimestampOnTicketCreate() {
        // Given
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject("Test");
        ticket.setDescription("Desc");

        // When - simulate @PrePersist
        ticket.onCreate();

        // Then
        assertThat(ticket.getCreatedAt()).isNotNull();
    }
}