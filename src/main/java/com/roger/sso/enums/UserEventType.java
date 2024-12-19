package com.roger.sso.enums;

public enum UserEventType {
  SIGN_IN(1),
  AUTHORIZED(2),
  SIGN_OUT(3);

  private final int code;

  UserEventType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
