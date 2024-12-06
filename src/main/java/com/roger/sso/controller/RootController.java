package com.roger.sso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.roger.sso.dto.SignUpDto;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/")
public class RootController {
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
      bindingResult.getAllErrors().forEach(error -> {
        System.out.println(error);
    });
      model.addAttribute("signUpDto", signUpDto);
      return "signUp";
    }

    return "signIn";
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
