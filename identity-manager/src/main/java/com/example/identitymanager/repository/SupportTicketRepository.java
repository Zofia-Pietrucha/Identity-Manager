package com.example.identitymanager.repository;

import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    // Derived query methods
    List<SupportTicket> findByUser(User user);

    List<SupportTicket> findByStatus(SupportTicket.TicketStatus status);

    // Custom @Query - find tickets by status with user data (JOIN FETCH for eager loading)
    @Query("SELECT t FROM SupportTicket t JOIN FETCH t.user WHERE t.status = :status")
    List<SupportTicket> findByStatusWithUser(@Param("status") SupportTicket.TicketStatus status);

    // Custom @Query - count tickets by user and status
    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.user.id = :userId AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") SupportTicket.TicketStatus status);

    // Custom @Query - find all tickets with user data (avoids N+1 problem)
    @Query("SELECT t FROM SupportTicket t JOIN FETCH t.user")
    List<SupportTicket> findAllWithUser();

    // Custom @Query - search tickets by subject pattern (case insensitive)
    @Query("SELECT t FROM SupportTicket t JOIN FETCH t.user WHERE LOWER(t.subject) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<SupportTicket> searchBySubject(@Param("pattern") String pattern);
}
