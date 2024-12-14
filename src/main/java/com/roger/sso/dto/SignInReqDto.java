package com.roger.sso.dto;

import com.roger.sso.validation.PasswordRules;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SignInReqDto {
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password is required")
  @PasswordRules
  private String password;

  private String redirect;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRedirect() {
    return redirect;
  }

  public void setRedirect(String redirect) {
    this.redirect = redirect;
  }
}
