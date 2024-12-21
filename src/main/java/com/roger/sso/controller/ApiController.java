package com.roger.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.roger.sso.service.UserService;

@RestController
@RequestMapping("/api")
public class ApiController {
  @Autowired
  private UserService userService;

  @GetMapping("/verify_auth_token")
  public boolean verifyAuthToken(
    @RequestParam("token") String token
  ) {
    return userService.verifyAuthToken(token);
  }

  @GetMapping("/signout")
  public String signOut(
    @RequestParam("token") String token
  ) {
    return "redirect:/signout";
  }
}
