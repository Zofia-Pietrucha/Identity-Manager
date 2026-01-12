package com.example.identitymanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private PhoneValidator phoneValidator;
    private NameValidator nameValidator;

    @BeforeEach
    void setUp() {
        phoneValidator = new PhoneValidator();
        nameValidator = new NameValidator();
    }

    // ==================== PHONE VALIDATOR TESTS ====================

    @Test
    void phoneValidator_shouldAcceptNull() {
        assertThat(phoneValidator.isValid(null, context)).isTrue();
    }

    @Test
    void phoneValidator_shouldAcceptBlank() {
        assertThat(phoneValidator.isValid("", context)).isTrue();
        assertThat(phoneValidator.isValid("   ", context)).isTrue();
    }

    @Test
    void phoneValidator_shouldAcceptValidPhoneNumbers() {
        assertThat(phoneValidator.isValid("123456789", context)).isTrue();
        assertThat(phoneValidator.isValid("+48123456789", context)).isTrue();
        assertThat(phoneValidator.isValid("+48 123 456 789", context)).isTrue();
        assertThat(phoneValidator.isValid("123-456-789", context)).isTrue();
        assertThat(phoneValidator.isValid("(12) 345-67-89", context)).isTrue();
        assertThat(phoneValidator.isValid("+1 (555) 123-4567", context)).isTrue();
    }

    @Test
    void phoneValidator_shouldRejectInvalidPhoneNumbers() {
        assertThat(phoneValidator.isValid("abc123", context)).isFalse();
        assertThat(phoneValidator.isValid("phone: 123", context)).isFalse();
        assertThat(phoneValidator.isValid("123@456", context)).isFalse();
        assertThat(phoneValidator.isValid("++48123", context)).isFalse();
    }

    // ==================== NAME VALIDATOR TESTS ====================

    @Test
    void nameValidator_shouldAcceptNull() {
        assertThat(nameValidator.isValid(null, context)).isTrue();
    }

    @Test
    void nameValidator_shouldAcceptBlank() {
        assertThat(nameValidator.isValid("", context)).isTrue();
        assertThat(nameValidator.isValid("   ", context)).isTrue();
    }

    @Test
    void nameValidator_shouldAcceptValidNames() {
        assertThat(nameValidator.isValid("John", context)).isTrue();
        assertThat(nameValidator.isValid("Anna Maria", context)).isTrue();
        assertThat(nameValidator.isValid("O'Connor", context)).isTrue();
        assertThat(nameValidator.isValid("Jean-Pierre", context)).isTrue();
        assertThat(nameValidator.isValid("Müller", context)).isTrue();
        assertThat(nameValidator.isValid("Żółć", context)).isTrue();
        assertThat(nameValidator.isValid("Иван", context)).isTrue();
    }

    @Test
    void nameValidator_shouldRejectInvalidNames() {
        assertThat(nameValidator.isValid("John123", context)).isFalse();
        assertThat(nameValidator.isValid("Anna@Maria", context)).isFalse();
        assertThat(nameValidator.isValid("Test!", context)).isFalse();
        assertThat(nameValidator.isValid("Name#1", context)).isFalse();
    }
}