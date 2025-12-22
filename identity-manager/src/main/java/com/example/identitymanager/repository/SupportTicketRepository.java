package com.example.identitymanager.repository;

import com.example.identitymanager.model.SupportTicket;
import com.example.identitymanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUser(User user);

    List<SupportTicket> findByStatus(SupportTicket.TicketStatus status);
}