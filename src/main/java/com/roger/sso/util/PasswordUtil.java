package com.roger.sso.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {
  private final PasswordEncoder encoder = new BCryptPasswordEncoder();

  public String encode(String password) {
    return encoder.encode(password);
  }

  public boolean matches(String rawPassword, String encodedPassword) {
    return encoder.matches(rawPassword, encodedPassword);
  }
}
