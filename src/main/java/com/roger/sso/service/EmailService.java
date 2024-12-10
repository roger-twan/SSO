package com.roger.sso.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.application.host}")
  private String host;

  public void sendEmail(
    String to,
    String subject,
    String text
  ) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }

  public void sendHtmlEmail(
    String to,
    String subject,
    String html
  ) {
    try {
      MimeMessage mineMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mineMessage, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(html, true);
  
      mailSender.send(mineMessage);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send email", e);
    }
  }

  public void sendHtmlActivationEmail(String to, String token) {
    String link = host + "/verify_email/" + token;
    String subject = "Account Activation - SSO";
    String html = "<p>Thank you for registering with SSO. Click the link below in 5 minutes to activate your account:</p>"
      + "<p><a href=\"" + link + "\">Activate Account</a></p>";
    sendHtmlEmail(to, subject, html);
  }
}
