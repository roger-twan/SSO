package com.roger.sso.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.roger.sso.interceptor.AuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Autowired
  private AuthInterceptor authInterceptor;

  @Override
  public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor)
        .addPathPatterns("/");
  }
}
