package com.roger.sso.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {
  @Autowired
  private EmailService emailService;

  @Test
  public void testSendActivationEmail() {
    emailService.sendActivationEmail("roger.twan@gmail.com","testToken");
  }
}
