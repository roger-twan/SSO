package com.roger.sso.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
  private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

  public static String encode(String password) {
    return encoder.encode(password);
  }

  public static boolean matches(String rawPassword, String encodedPassword) {
    return encoder.matches(rawPassword, encodedPassword);
  }
}
