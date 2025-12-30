package com.example.identitymanager.repository;

import com.example.identitymanager.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    // SELECT operations
    List<User> findAllUsers();

    Optional<User> findUserById(Long id);

    Optional<User> findUserByEmail(String email);

    int countUsers();

    List<User> findUsersByPrivacyEnabled(boolean privacyEnabled);

    // INSERT operation
    int insertUser(User user);

    void executeUpdate(String sql, Object... params);

    // UPDATE operation
    int updateUser(User user);

    // DELETE operation
    int deleteUserById(Long id);
}