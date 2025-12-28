package com.example.identitymanager.service;

import com.example.identitymanager.dto.UserDTO;
import com.example.identitymanager.dto.UserRegistrationDTO;
import com.example.identitymanager.dto.UserUpdateDTO;
import com.example.identitymanager.exception.ResourceNotFoundException;
import com.example.identitymanager.model.Role;
import com.example.identitymanager.model.User;
import com.example.identitymanager.repository.RoleRepository;
import com.example.identitymanager.repository.UserRepository;
import com.example.identitymanager.exception.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Create new user
    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new DuplicateResourceException("User", "email", registrationDTO.getEmail());
        }

        // Create new user entity
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setPhone(registrationDTO.getPhone());
        user.setIsPrivacyEnabled(registrationDTO.getIsPrivacyEnabled());

        // Assign default USER role
        Optional<Role> userRole = roleRepository.findByName(Role.RoleName.USER);
        if (userRole.isPresent()) {
            user.getRoles().add(userRole.get());
        }

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // Get all users
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get user by ID
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Get user by email
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    // Update user profile (firstName, lastName, phone) - for /api/me
    public UserDTO updateUserProfile(String email, UserUpdateDTO updateDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setFirstName(updateDTO.getFirstName());
        user.setLastName(updateDTO.getLastName());
        user.setPhone(updateDTO.getPhone());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // Update user by ID - for /api/users/{id}
    public UserDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setFirstName(updateDTO.getFirstName());
        user.setLastName(updateDTO.getLastName());
        user.setPhone(updateDTO.getPhone());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // Delete user by ID
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);
    }

    // Update privacy settings
    public UserDTO updatePrivacySettings(String email, boolean isPrivacyEnabled) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setIsPrivacyEnabled(isPrivacyEnabled);

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // Convert User entity to DTO
    private UserDTO convertToDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getIsPrivacyEnabled(),
                roleNames,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // Create user WITHOUT encoding password (for data.sql imports with pre-hashed passwords)
    @Transactional
    public User createUserWithEncodedPassword(User user) {
        return userRepository.save(user);
    }
}