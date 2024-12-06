package com.roger.sso.validation;

import com.roger.sso.dto.SignUpDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, SignUpDto> {

  @Override
  public boolean isValid(SignUpDto signUpDto, ConstraintValidatorContext context) {
    if (signUpDto.getPassword() == null || signUpDto.getConfirmPassword() == null) {
      return false;
    }
    return signUpDto.getPassword().equals(signUpDto.getConfirmPassword());
  }
}
