package com.roger.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.roger.sso.dto.SignUpDto;
import com.roger.sso.exception.VerificationException;
import com.roger.sso.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/signup")
public class SignUpController {
  @Autowired
  private UserService userService;

  @GetMapping
  public String getSignUpPage(Model model) {
    model.addAttribute("signUpDto", new SignUpDto());
    return "signUp";
  }

  @PostMapping
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
  public String getVerifyEmailPage(@PathVariable String token, Model model) {
    String result = "SUCCESS";
    try {
      userService.verifyEmail(token);
    } catch (VerificationException e) {
      switch (e.getError()) {
        case USER_NOT_FOUND:
          result = "USER_NOT_FOUND";
          break;
        case ALREADY_VERIFIED:
          result = "ALREADY_VERIFIED";
          break;
        case INVALID_TOKEN:
          result = "INVALID_TOKEN";
          break;
        case TOKEN_EXPIRED:
          result = "TOKEN_EXPIRED";
          break;
        default:
          break;
      }
    }
    model.addAttribute("result", result);
    return "verifyEmail";
  }
}
