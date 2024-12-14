package com.roger.sso.dto;

import com.roger.sso.validation.PasswordMatches;
import com.roger.sso.validation.PasswordRules;

import jakarta.validation.constraints.NotBlank;

@PasswordMatches
public class SignUpDto extends SignInReqDto {
  @NotBlank(message = "Confirm Password is required")
  @PasswordRules
  private String confirmPassword;

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }
}
