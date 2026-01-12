package com.example.identitymanager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {
    String message() default "Must contain only letters, spaces, hyphens or apostrophes";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}