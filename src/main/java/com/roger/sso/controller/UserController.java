package com.roger.sso.controller;

import com.roger.sso.entity.User;
import com.roger.sso.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
  @Autowired
  private UserService userService;

  @GetMapping("/view")
  public String getUserPage() {
    return "userView";
  }

  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @PostMapping
  public User createUser(@RequestBody User user) {
    return userService.createUser(user);
  }
}
