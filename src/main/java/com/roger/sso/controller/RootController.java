package com.roger.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.roger.sso.dto.UserInfoDto;
import com.roger.sso.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/")
public class RootController {
  @Autowired
  private UserService userService;

  @GetMapping
  public String getHomePage(
    @CookieValue(value = "authToken", defaultValue = "") String authToken,
    Model model
  ) {
    UserInfoDto userInfoDto = userService.getUserInfo(authToken);
    model.addAttribute("userInfo", userInfoDto);
    return "home";
  }

  @GetMapping("signout")
  public String redirectToSignInPage(
    @CookieValue(value = "authToken", defaultValue = "") String authToken,
    HttpServletResponse response
  ) {
    userService.signOut(authToken);

    Cookie authCookie = new Cookie("authToken", "");
    authCookie.setPath("/");
    authCookie.setHttpOnly(true);
    authCookie.setMaxAge(0);

    response.addCookie(authCookie);

    return "redirect:/signin";
  }
}
