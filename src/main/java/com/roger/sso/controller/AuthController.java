package com.roger.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.roger.sso.service.UserService;

@Controller
@RequestMapping("/auth")
public class AuthController {
  @Autowired
  private UserService userService;

  @GetMapping
  public String getAuthPage(
    @RequestParam(required = true) String redirect,
    @CookieValue(value = "authToken", defaultValue = "") String authToken,
    Model model
  ) {
    if (authToken.isEmpty()) {
      model.addAttribute("errorMessage", "Please sign in first");
      return "auth";
    }

    if (!redirect.startsWith("http://") && !redirect.startsWith("https://")) {
      model.addAttribute("errorMessage", "Invalid redirect URL");
      return "auth";
    }
    
    try {
      String host = redirect.split("://")[1].split("/")[0];
      Boolean isAuthorized = userService.verifyAuthorized(authToken, host);
      
      if (isAuthorized) {
        return "redirect:" + redirect + "?authToken=" + authToken;
      } else {
        model.addAttribute("authLink", "/auth/add?host=" + host + "&redirect=" + redirect);
        return "auth";
      }
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "auth";
    }
  }

  @GetMapping("/add")
  public String addAuthHost(
    @RequestParam(required = true) String host,
    @RequestParam(required = true) String redirect,
    @CookieValue(value = "authToken", defaultValue = "") String authToken,
    Model model
  ) {
    if (authToken.isEmpty()) {
      model.addAttribute("errorMessage", "Please sign in first");
      return "auth";
    }

    if (!redirect.startsWith("http://") && !redirect.startsWith("https://")) {
      model.addAttribute("errorMessage", "Invalid redirect URL");
      return "auth";
    }

    try {
      userService.addAuthHost(authToken, host);
      return "redirect:" + redirect + "?authToken=" + authToken;
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "auth";
    }
  }
}
