package com.roger.sso.dto;

public class SignInResDto {
  private String token;
  private int authExpirationDays;

  public SignInResDto(String token, int authExpirationDays) {
    this.token = token;
    this.authExpirationDays = authExpirationDays;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public int getAuthExpirationDays() {
    return authExpirationDays;
  }

  public void setExpirationTime(int authExpirationDays) {
    this.authExpirationDays = authExpirationDays;
  }
}
