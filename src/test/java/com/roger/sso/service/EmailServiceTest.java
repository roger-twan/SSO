package com.roger.sso.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {
  @Autowired
  private EmailService emailService;

  @Test
  public void testSendEmail() {
    emailService.sendEmail("roger.twan@gmail.com", "Test Email", "This is a test email. Time: " + LocalDateTime.now());
  }
}
