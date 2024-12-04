package com.roger.sso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class PageController {
  @GetMapping
  public String getHomePage() {
    return "home";
  }

  @GetMapping("/signin")
  public String getSignInPage() {
    return "signIn";
  }

  @GetMapping("/signup")
  public String getSignUpPage() {
    return "signUp";
  }

  @GetMapping("/verify_email")
  public String getVerifyEmailPage() {
    return "verifyEmail";
  }

  @GetMapping("/verify_email_result")
  public String getVerifyEmailResultPage() {
    return "verifyEmailResult";
  }

  @GetMapping("/authorization")
  public String getAuthorizationPage() {
    return "authorization";
  }
}
