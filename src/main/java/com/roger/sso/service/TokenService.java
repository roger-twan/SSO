package com.roger.sso.service;

import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;

@Service
public class TokenService {
  @Value("${jwt.secret-key}")
  private String secretKeyString;
  private SecretKey secretKey;

  @PostConstruct
  public void init() {
    this.secretKey = new SecretKeySpec(secretKeyString.getBytes(), "HmacSHA256");
  }

  public String generateToken(String subject, Map<String, Object> claims) {
    return Jwts.builder()
      .setSubject(subject)
      .addClaims(claims)
      .signWith(secretKey)
      .compact();
  }

  public String generateToken(String subject) {
    return generateToken(subject, null);
  }

  public Claims parseToken(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(secretKey)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }
}
