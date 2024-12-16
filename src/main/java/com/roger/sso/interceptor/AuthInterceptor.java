package com.roger.sso.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpServletRequest;

import com.roger.sso.service.RedisService;

@Component
public class AuthInterceptor implements HandlerInterceptor {
  @Autowired
  private RedisService redisService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String authToken = null;
    String queries = request.getQueryString() == null ? "" : "?" + request.getQueryString();

    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("authToken".equals(cookie.getName())) {
          authToken = cookie.getValue();
          break;
        }
      }
    }

    if (authToken == null || authToken.isEmpty()) {
      response.sendRedirect("/signin" + queries);
      return false;
    }

    String redisToken =redisService.getAuthTokenRedis(authToken);
    if (redisToken == null || !redisToken.equals(authToken)) {
      response.sendRedirect("/signin" + queries);
      return false;
    }

    return true;
  }
}
