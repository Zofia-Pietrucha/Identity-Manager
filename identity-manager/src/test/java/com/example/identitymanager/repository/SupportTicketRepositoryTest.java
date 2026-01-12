package com.example.identitymanager.repository;

import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.SupportTicket;
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
class SupportTicketRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SupportTicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;
    private SupportTicket testTicket;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = new User();
        testUser.setEmail("user@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsPrivacyEnabled(false);
        entityManager.persist(testUser);

        anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setIsPrivacyEnabled(false);
        entityManager.persist(anotherUser);

        // Create test ticket
        testTicket = new SupportTicket();
        testTicket.setSubject("Test Issue");
        testTicket.setDescription("Test Description");
        testTicket.setStatus(SupportTicket.TicketStatus.OPEN);
        testTicket.setUser(testUser);
        entityManager.persistAndFlush(testTicket);
    }

    // ==================== BASIC CRUD TESTS ====================

    @Test
    void shouldSaveTicket() {
        // Given
        SupportTicket newTicket = new SupportTicket();
        newTicket.setSubject("New Issue");
        newTicket.setDescription("New Description");
        newTicket.setStatus(SupportTicket.TicketStatus.OPEN);
        newTicket.setUser(testUser);

        // When
        SupportTicket saved = ticketRepository.save(newTicket);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSubject()).isEqualTo("New Issue");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindTicketById() {
        // When
        Optional<SupportTicket> found = ticketRepository.findById(testTicket.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSubject()).isEqualTo("Test Issue");
        assertThat(found.get().getUser().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void shouldReturnEmptyWhenTicketNotFound() {
        // When
        Optional<SupportTicket> found = ticketRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllTickets() {
        // Given
        SupportTicket ticket2 = new SupportTicket();
        ticket2.setSubject("Second Issue");
        ticket2.setDescription("Second Description");
        ticket2.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        ticket2.setUser(anotherUser);
        entityManager.persistAndFlush(ticket2);

        // When
        List<SupportTicket> tickets = ticketRepository.findAll();

        // Then
        assertThat(tickets).hasSize(2);
        assertThat(tickets).extracting(SupportTicket::getSubject)
                .containsExactlyInAnyOrder("Test Issue", "Second Issue");
    }

    @Test
    void shouldDeleteTicket() {
        // When
        ticketRepository.delete(testTicket);
        entityManager.flush();

        Optional<SupportTicket> found = ticketRepository.findById(testTicket.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateTicket() {
        // Given
        testTicket.setSubject("Updated Subject");
        testTicket.setStatus(SupportTicket.TicketStatus.RESOLVED);

        // When
        SupportTicket updated = ticketRepository.save(testTicket);
        entityManager.flush();
        entityManager.clear();

        SupportTicket found = ticketRepository.findById(updated.getId()).orElseThrow();

        // Then
        assertThat(found.getSubject()).isEqualTo("Updated Subject");
        assertThat(found.getStatus()).isEqualTo(SupportTicket.TicketStatus.RESOLVED);
    }

    // ==================== FIND BY USER TESTS ====================

    @Test
    void shouldFindTicketsByUser() {
        // Given - create more tickets for testUser
        SupportTicket ticket2 = new SupportTicket();
        ticket2.setSubject("Second User Issue");
        ticket2.setDescription("Description");
        ticket2.setStatus(SupportTicket.TicketStatus.OPEN);
        ticket2.setUser(testUser);
        entityManager.persist(ticket2);

        // Create ticket for another user
        SupportTicket anotherTicket = new SupportTicket();
        anotherTicket.setSubject("Another User Issue");
        anotherTicket.setDescription("Description");
        anotherTicket.setStatus(SupportTicket.TicketStatus.OPEN);
        anotherTicket.setUser(anotherUser);
        entityManager.persistAndFlush(anotherTicket);

        // When
        List<SupportTicket> userTickets = ticketRepository.findByUser(testUser);

        // Then
        assertThat(userTickets).hasSize(2);
        assertThat(userTickets).allMatch(t -> t.getUser().getId().equals(testUser.getId()));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoTickets() {
        // Given - create new user without tickets
        User noTicketsUser = new User();
        noTicketsUser.setEmail("notickets@example.com");
        noTicketsUser.setPassword("password");
        noTicketsUser.setFirstName("No");
        noTicketsUser.setLastName("Tickets");
        noTicketsUser.setIsPrivacyEnabled(false);
        entityManager.persistAndFlush(noTicketsUser);

        // When
        List<SupportTicket> tickets = ticketRepository.findByUser(noTicketsUser);

        // Then
        assertThat(tickets).isEmpty();
    }

    // ==================== FIND BY STATUS TESTS ====================

    @Test
    void shouldFindTicketsByStatus() {
        // Given
        SupportTicket inProgressTicket = new SupportTicket();
        inProgressTicket.setSubject("In Progress Issue");
        inProgressTicket.setDescription("Description");
        inProgressTicket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        inProgressTicket.setUser(testUser);
        entityManager.persist(inProgressTicket);

        SupportTicket resolvedTicket = new SupportTicket();
        resolvedTicket.setSubject("Resolved Issue");
        resolvedTicket.setDescription("Description");
        resolvedTicket.setStatus(SupportTicket.TicketStatus.RESOLVED);
        resolvedTicket.setUser(anotherUser);
        entityManager.persistAndFlush(resolvedTicket);

        // When
        List<SupportTicket> openTickets = ticketRepository.findByStatus(SupportTicket.TicketStatus.OPEN);
        List<SupportTicket> inProgressTickets = ticketRepository.findByStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        List<SupportTicket> resolvedTickets = ticketRepository.findByStatus(SupportTicket.TicketStatus.RESOLVED);

        // Then
        assertThat(openTickets).hasSize(1);
        assertThat(openTickets.get(0).getSubject()).isEqualTo("Test Issue");

        assertThat(inProgressTickets).hasSize(1);
        assertThat(inProgressTickets.get(0).getSubject()).isEqualTo("In Progress Issue");

        assertThat(resolvedTickets).hasSize(1);
        assertThat(resolvedTickets.get(0).getSubject()).isEqualTo("Resolved Issue");
    }

    @Test
    void shouldReturnEmptyListWhenNoTicketsWithStatus() {
        // When
        List<SupportTicket> closedTickets = ticketRepository.findByStatus(SupportTicket.TicketStatus.CLOSED);

        // Then
        assertThat(closedTickets).isEmpty();
    }

    @Test
    void shouldFindAllTicketsWithSameStatus() {
        // Given - create multiple open tickets
        for (int i = 0; i < 5; i++) {
            SupportTicket ticket = new SupportTicket();
            ticket.setSubject("Open Issue " + i);
            ticket.setDescription("Description");
            ticket.setStatus(SupportTicket.TicketStatus.OPEN);
            ticket.setUser(testUser);
            entityManager.persist(ticket);
        }
        entityManager.flush();

        // When
        List<SupportTicket> openTickets = ticketRepository.findByStatus(SupportTicket.TicketStatus.OPEN);

        // Then
        assertThat(openTickets).hasSize(6); // 5 new + 1 from setUp
    }

    // ==================== STATUS TRANSITION TESTS ====================

    @Test
    void shouldUpdateTicketStatusFromOpenToInProgress() {
        // When
        testTicket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        ticketRepository.save(testTicket);
        entityManager.flush();
        entityManager.clear();

        SupportTicket found = ticketRepository.findById(testTicket.getId()).orElseThrow();

        // Then
        assertThat(found.getStatus()).isEqualTo(SupportTicket.TicketStatus.IN_PROGRESS);
    }

    @Test
    void shouldUpdateTicketStatusFromInProgressToResolved() {
        // Given
        testTicket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        entityManager.persistAndFlush(testTicket);

        // When
        testTicket.setStatus(SupportTicket.TicketStatus.RESOLVED);
        ticketRepository.save(testTicket);
        entityManager.flush();
        entityManager.clear();

        SupportTicket found = ticketRepository.findById(testTicket.getId()).orElseThrow();

        // Then
        assertThat(found.getStatus()).isEqualTo(SupportTicket.TicketStatus.RESOLVED);
    }

    @Test
    void shouldUpdateTicketStatusToClosed() {
        // When
        testTicket.setStatus(SupportTicket.TicketStatus.CLOSED);
        ticketRepository.save(testTicket);
        entityManager.flush();
        entityManager.clear();

        SupportTicket found = ticketRepository.findById(testTicket.getId()).orElseThrow();

        // Then
        assertThat(found.getStatus()).isEqualTo(SupportTicket.TicketStatus.CLOSED);
    }

    // ==================== RELATIONSHIP TESTS ====================

    @Test
    void shouldLoadUserWithTicket() {
        // When
        entityManager.clear(); // Clear cache to force reload
        SupportTicket found = ticketRepository.findById(testTicket.getId()).orElseThrow();

        // Then
        assertThat(found.getUser()).isNotNull();
        assertThat(found.getUser().getEmail()).isEqualTo("user@example.com");
        assertThat(found.getUser().getFirstName()).isEqualTo("Test");
    }

    @Test
    void shouldNotDeleteUserWhenDeletingTicket() {
        // Given
        Long userId = testUser.getId();

        // When
        ticketRepository.delete(testTicket);
        entityManager.flush();

        // Then - user should still exist
        Optional<User> user = userRepository.findById(userId);
        assertThat(user).isPresent();
    }

    // ==================== TIMESTAMP TESTS ====================

    @Test
    void shouldSetCreatedAtOnSave() {
        // Given
        SupportTicket newTicket = new SupportTicket();
        newTicket.setSubject("New Issue");
        newTicket.setDescription("Description");
        newTicket.setStatus(SupportTicket.TicketStatus.OPEN);
        newTicket.setUser(testUser);

        // When
        SupportTicket saved = ticketRepository.save(newTicket);
        entityManager.flush();

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindByStatusWithUser() {
        // Given
        SupportTicket inProgressTicket = new SupportTicket();
        inProgressTicket.setSubject("In Progress Issue");
        inProgressTicket.setDescription("Description");
        inProgressTicket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        inProgressTicket.setUser(testUser);
        entityManager.persistAndFlush(inProgressTicket);

        // When
        List<SupportTicket> result = ticketRepository.findByStatusWithUser(SupportTicket.TicketStatus.OPEN);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubject()).isEqualTo("Test Issue");
        assertThat(result.get(0).getUser()).isNotNull();
        assertThat(result.get(0).getUser().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void shouldReturnEmptyListWhenNoTicketsWithStatusAndUser() {
        // When
        List<SupportTicket> result = ticketRepository.findByStatusWithUser(SupportTicket.TicketStatus.CLOSED);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCountByUserIdAndStatus() {
        // Given - create additional tickets
        SupportTicket openTicket2 = new SupportTicket();
        openTicket2.setSubject("Second Open Issue");
        openTicket2.setDescription("Description");
        openTicket2.setStatus(SupportTicket.TicketStatus.OPEN);
        openTicket2.setUser(testUser);
        entityManager.persist(openTicket2);

        SupportTicket resolvedTicket = new SupportTicket();
        resolvedTicket.setSubject("Resolved Issue");
        resolvedTicket.setDescription("Description");
        resolvedTicket.setStatus(SupportTicket.TicketStatus.RESOLVED);
        resolvedTicket.setUser(testUser);
        entityManager.persistAndFlush(resolvedTicket);

        // When
        long openCount = ticketRepository.countByUserIdAndStatus(testUser.getId(), SupportTicket.TicketStatus.OPEN);
        long resolvedCount = ticketRepository.countByUserIdAndStatus(testUser.getId(), SupportTicket.TicketStatus.RESOLVED);
        long closedCount = ticketRepository.countByUserIdAndStatus(testUser.getId(), SupportTicket.TicketStatus.CLOSED);

        // Then
        assertThat(openCount).isEqualTo(2); // testTicket + openTicket2
        assertThat(resolvedCount).isEqualTo(1);
        assertThat(closedCount).isEqualTo(0);
    }

    @Test
    void shouldReturnZeroCountWhenUserHasNoTicketsWithStatus() {
        // When
        long count = ticketRepository.countByUserIdAndStatus(anotherUser.getId(), SupportTicket.TicketStatus.OPEN);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldFindAllWithUser() {
        // Given - create ticket for another user
        SupportTicket anotherTicket = new SupportTicket();
        anotherTicket.setSubject("Another User Issue");
        anotherTicket.setDescription("Description");
        anotherTicket.setStatus(SupportTicket.TicketStatus.OPEN);
        anotherTicket.setUser(anotherUser);
        entityManager.persistAndFlush(anotherTicket);

        // When
        List<SupportTicket> result = ticketRepository.findAllWithUser();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getUser() != null);
    }

    @Test
    void shouldSearchBySubject() {
        // Given
        SupportTicket loginTicket = new SupportTicket();
        loginTicket.setSubject("Login Problem");
        loginTicket.setDescription("Cannot login");
        loginTicket.setStatus(SupportTicket.TicketStatus.OPEN);
        loginTicket.setUser(testUser);
        entityManager.persistAndFlush(loginTicket);

        // When
        List<SupportTicket> result = ticketRepository.searchBySubject("Login");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubject()).isEqualTo("Login Problem");
    }

    @Test
    void shouldSearchBySubjectCaseInsensitive() {
        // When - search with different case
        List<SupportTicket> result = ticketRepository.searchBySubject("test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubject()).isEqualTo("Test Issue");
    }

    @Test
    void shouldSearchBySubjectPartialMatch() {
        // When - search with partial match
        List<SupportTicket> result = ticketRepository.searchBySubject("Issue");

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenSearchBySubjectNoMatch() {
        // When
        List<SupportTicket> result = ticketRepository.searchBySubject("NonExistent");

        // Then
        assertThat(result).isEmpty();
    }
}