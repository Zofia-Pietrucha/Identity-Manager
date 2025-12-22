package com.example.identitymanager.repository;

import com.example.identitymanager.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    List<User> findAllUsers();

    Optional<User> findUserById(Long id);

    Optional<User> findUserByEmail(String email);

    int countUsers();

    List<User> findUsersByPrivacyEnabled(boolean privacyEnabled);
}