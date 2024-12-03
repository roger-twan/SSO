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
  public String getSignUpPagez() {
    return "signUp";
  }
}
