package com.roger.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.roger.sso.dto.SignInReqDto;
import com.roger.sso.dto.SignInResDto;
import com.roger.sso.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/signin")
public class SignInController {
  @Autowired
  private UserService userService;

  @GetMapping
  public String getSignInPage(
    @RequestParam(required = false) String redirect,
    @CookieValue(value = "authToken", defaultValue = "") String authToken,
    Model model
  ) {
    if (!authToken.isEmpty() && userService.getAuthStatus(authToken)) {
      if (redirect == null || redirect.isEmpty()) {
        return "redirect:/";
      } else {
        return "redirect:/authorization?redirect=" + redirect;
      }
    }

    SignInReqDto signInReqDto = new SignInReqDto();
    signInReqDto.setRedirect(redirect);
    model.addAttribute("signInReqDto",signInReqDto);
    return "signIn";
  }

  @PostMapping
  public String handleSignIn(
    @Valid @ModelAttribute("signInReqDto") SignInReqDto signInReqDto,
    BindingResult bindingResult,
    Model model,
    HttpServletResponse response
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("signInReqDto", signInReqDto);
      return "signIn";
    }

    try {
      SignInResDto signInRes = userService.handleSignIn(signInReqDto);
      String token = signInRes.getToken();
      int authExpirationDays = signInRes.getAuthExpirationDays();
      String redirect = signInReqDto.getRedirect();

      Cookie cookie = new Cookie("authToken", token);
      cookie.setHttpOnly(true);
      cookie.setSecure(true);
      cookie.setPath("/");
      cookie.setMaxAge(60 * 60 * 24 * authExpirationDays);

      response.addCookie(cookie);

      if (redirect == null || redirect.isEmpty()) {
        return "redirect:/";
      } else {
        return "redirect:/authorization?redirect=" + redirect;
      }
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "signIn";
    }
  }
}
