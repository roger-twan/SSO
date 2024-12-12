package com.roger.sso.exception;

import com.roger.sso.enums.VerificationError;

public class VerificationException extends RuntimeException {
  private final VerificationError error;

  public VerificationException(VerificationError error) {
    super(error.getMessage());
    this.error = error;
  }

  public VerificationError getError() {
    return error;
  }
}
