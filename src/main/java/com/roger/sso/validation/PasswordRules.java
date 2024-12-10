package com.roger.sso.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordRulesValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordRules {
    String message() default "Password must be at least 8 characters, with uppercase, lowercase, and a number.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
