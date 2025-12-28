package com.example.identitymanager.repository;

import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Derived query methods
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Pageable support - returns Page with pagination metadata
    Page<User> findAll(Pageable pageable);

    // Custom @Query - find users by role name with JPQL (FIXED - accepts enum)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRoleName(@Param("roleName") Role.RoleName roleName);

    // Custom @Query - count users with privacy enabled
    @Query("SELECT COUNT(u) FROM User u WHERE u.isPrivacyEnabled = true")
    long countUsersWithPrivacyEnabled();

    // Custom @Query - find users by name pattern (case insensitive)
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :pattern, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<User> searchUsersByName(@Param("pattern") String pattern);

    // Custom @Query with Pageable - search with pagination
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}