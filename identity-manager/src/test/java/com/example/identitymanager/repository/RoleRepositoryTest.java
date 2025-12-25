package com.example.identitymanager.repository;

import com.example.identitymanager.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldFindRoleByName() {
        // Given
        Role role = new Role();
        role.setName(Role.RoleName.USER);
        roleRepository.save(role);

        // When
        Optional<Role> found = roleRepository.findByName(Role.RoleName.USER);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(Role.RoleName.USER);
    }

    @Test
    void shouldReturnEmptyWhenRoleNameNotFound() {
        // When
        Optional<Role> found = roleRepository.findByName(Role.RoleName.ADMIN);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveRole() {
        // Given
        Role adminRole = new Role();
        adminRole.setName(Role.RoleName.ADMIN);

        // When
        Role saved = roleRepository.save(adminRole);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(Role.RoleName.ADMIN);
    }
}