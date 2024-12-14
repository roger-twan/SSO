package com.roger.sso.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {
  @Value("${spring.mail.test.receiver}")
  private String testEmailReceiver;

  @Autowired
  private EmailService emailService;

  @Test
  public void testSendActivationEmail() {
    emailService.sendActivationEmail(testEmailReceiver,"testToken");
  }
}
