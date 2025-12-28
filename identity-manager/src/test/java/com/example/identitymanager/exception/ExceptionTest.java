package com.example.identitymanager.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void shouldCreateResourceNotFoundException() {
        // When
        ResourceNotFoundException exception =
                new ResourceNotFoundException("Test message");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Test message");
    }

    @Test
    void shouldCreateResourceNotFoundExceptionWithFields() {
        // When
        ResourceNotFoundException exception =
                new ResourceNotFoundException("User", "id", 123);

        // Then
        assertThat(exception.getMessage())
                .isEqualTo("User not found with id: '123'");
    }

    @Test
    void shouldCreateDuplicateResourceException() {
        // When
        DuplicateResourceException exception =
                new DuplicateResourceException("Test duplicate");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Test duplicate");
    }

    @Test
    void shouldCreateDuplicateResourceExceptionWithFields() {
        // When
        DuplicateResourceException exception =
                new DuplicateResourceException("User", "email", "test@test.com");

        // Then
        assertThat(exception.getMessage())
                .isEqualTo("User already exists with email: 'test@test.com'");
    }

    @Test
    void shouldCreateErrorResponse() {
        // When
        ErrorResponse response = new ErrorResponse();
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage("Resource not found");

        // Then
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getError()).isEqualTo("Not Found");
        assertThat(response.getMessage()).isEqualTo("Resource not found");
    }
}