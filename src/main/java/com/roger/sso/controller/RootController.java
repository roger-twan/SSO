package com.roger.sso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class RootController {
  @GetMapping
  public String getHomePage() {
    return "home";
  }

  @GetMapping("/authorization")
  public String getAuthorizationPage() {
    return "authorization";
  }
}
