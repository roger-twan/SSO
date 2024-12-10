package com.roger.sso.dto;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import static org.assertj.core.api.Assertions.assertThat;

public class SignUpDtoValidationTest {
  private Validator validator;

  @BeforeEach
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testValidSignUpDto() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("test@example.com");
    signUpDto.setPassword("Valid123");
    signUpDto.setConfirmPassword("Valid123");

    Set<ConstraintViolation<SignUpDto>> violations = validator.validate(signUpDto);

    assertThat(violations).isEmpty();
  }

  @Test
  public void testInvalidEmail() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("invalidemail");
    signUpDto.setPassword("Valid123");
    signUpDto.setConfirmPassword("Valid123");

    Set<ConstraintViolation<SignUpDto>> violations = validator.validate(signUpDto);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
  }

  @Test
  public void testPasswordTooShort() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("test@example.com");
    signUpDto.setPassword("123");
    signUpDto.setConfirmPassword("123");

    Set<ConstraintViolation<SignUpDto>> violations = validator.validate(signUpDto);

    assertThat(violations).hasSize(2);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Password must be at least 8 characters, with uppercase, lowercase, and a number.");
  }

  @Test
  public void testPasswordMismatch() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("test@example.com");
    signUpDto.setPassword("Password1");
    signUpDto.setConfirmPassword("Password2");

    Set<ConstraintViolation<SignUpDto>> violations = validator.validate(signUpDto);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Passwords do not match");
  }
}
