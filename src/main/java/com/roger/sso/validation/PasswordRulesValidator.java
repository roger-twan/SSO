package com.roger.sso.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordRulesValidator implements ConstraintValidator<PasswordRules, String> {
  private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null) {
      return false;
    }
    return password.matches(PASSWORD_PATTERN);
  }
}
