package com.roger.sso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.jsonwebtoken.Claims;

@SpringBootTest
public class TokenServiceTest {
  @Autowired
  private TokenService tokenService;

  @Test
  public void testGenerateTokenWithClaim() {
    String subject = "testSubject";
    Map<String, Object> claims = new HashMap<>();
    claims.put("testKey", "testValue");
    String token = tokenService.generateToken(subject, claims);
    assertNotNull(token);

    Claims parsedClaims = tokenService.parseToken(token);
    assertThat(parsedClaims.getSubject()).isEqualTo(subject);
    assertThat(parsedClaims.get("testKey")).isEqualTo("testValue");
  }

  @Test
  public void testGenerateTokenWithoutClaim() {
    String subject = "testSubject";
    String token = tokenService.generateToken(subject);
    assertNotNull(token);

    Claims parsedClaims = tokenService.parseToken(token);
    assertThat(parsedClaims.getSubject()).isEqualTo(subject);
  }
}
