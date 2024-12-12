package com.roger.sso.enums;

public enum VerificationError {
  USER_NOT_FOUND("User not found."),
  ALREADY_VERIFIED("Account already verified."),
  TOKEN_EXPIRED("Token expired."),
  INVALID_TOKEN("Invalid token.");

  private final String message;

  VerificationError(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
