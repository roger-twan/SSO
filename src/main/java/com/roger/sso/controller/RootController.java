package com.roger.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.roger.sso.dto.SignUpDto;
import com.roger.sso.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/")
public class RootController {
  @Autowired
  private UserService userService;

  @GetMapping
  public String getHomePage() {
    return "home";
  }

  @GetMapping("/signin")
  public String getSignInPage() {
    return "signIn";
  }

  @GetMapping("/signup")
  public String getSignUpPage(Model model) {
    model.addAttribute("signUpDto", new SignUpDto());
    return "signUp";
  }

  @PostMapping("/signup")
  public String handleSignUp(
    @Valid @ModelAttribute("signUpDto") SignUpDto signUpDto,
    BindingResult bindingResult,
    Model model
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("signUpDto", signUpDto);
      return "signUp";
    }

    try {
      userService.handleSignUp(signUpDto);
      return "emailSent";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "signUp";
    }
  }

  @GetMapping("/email_sent")
  public String getVerifyEmailSentPage() {
    return "emailSent";
  }

  @GetMapping("/verify_email/{token}")
  public String getVerifyEmailPage(@PathVariable String token) {
    // TODO: verify token
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
