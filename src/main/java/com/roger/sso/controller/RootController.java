package com.roger.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class RootController {
  @Autowired

  @GetMapping
  public String getHomePage() {
    return "home";
  }

  @GetMapping("/signin")
  public String getSignInPage() {
    return "signIn";
  }

  @GetMapping("/authorization")
  public String getAuthorizationPage() {
    return "authorization";
  }
}
