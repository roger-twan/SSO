package com.roger.sso.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.roger.sso.service.UserService;

import jakarta.servlet.http.Cookie;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @Test
  public void getAuthPageWithNoAuthToken() throws Exception {
    mockMvc.perform(get("/auth")
            .param("redirect", "https://google.com")
            .cookie(new Cookie("test", "test")))
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attribute("errorMessage", "Please sign in first"));
  }

  @Test
  public void getAuthPageWithInvalidRedirect() throws Exception {
    mockMvc.perform(get("/auth")
            .param("redirect", "invalid-url")
            .cookie(new Cookie("authToken", "validToken")))
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attributeExists("errorMessage"));
  }

  @Test
  public void getAuthPageWithAuthorizedHost() throws Exception {
    when(userService.verifyAuthorized(anyString(), anyString())).thenReturn(true);

    mockMvc.perform(get("/auth")
            .param("redirect", "https://google.com/path")
            .cookie(new Cookie("authToken", "validToken")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("https://google.com/path?authToken=validToken"));
  }

  @Test
  public void getAuthPageWithNotAuthorizedHost() throws Exception {
    when(userService.verifyAuthorized(anyString(), anyString())).thenReturn(false);

    mockMvc.perform(get("/auth")
            .param("redirect", "https://google.com/path")
            .cookie(new Cookie("authToken", "validToken")))
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attributeExists("authLink"));
  }

  @Test
  public void addAuthHostWithNoAuthToken() throws Exception {
    mockMvc.perform(get("/auth/add")
            .param("host", "example.com")
            .param("redirect", "https://google.com/path")
            .cookie(new Cookie("test", "test")))
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attribute("errorMessage", "Please sign in first"));
  }

  @Test
  public void addAuthHostWithInvalidRedirect() throws Exception {
    mockMvc.perform(get("/auth/add")
            .param("host", "https://google.com")
            .param("redirect", "invalid-url")
            .cookie(new Cookie("authToken", "validToken")))
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attribute("errorMessage", "Invalid redirect URL"));
  }

  @Test
  public void addAuthHostWithSuccess() throws Exception {
    doNothing().when(userService).addAuthHost(anyString(), anyString());

    mockMvc.perform(get("/auth/add")
            .param("host", "example.com")
            .param("redirect", "https://google.com/path")
            .cookie(new Cookie("authToken", "validToken")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("https://google.com/path?authToken=validToken"));
  }

  @Test
  public void addAuthHostWithInvalidHost() throws Exception {
    doThrow(new IllegalArgumentException("Invalid token")).when(userService).addAuthHost(anyString(), anyString());

    mockMvc.perform(get("/auth/add")
            .param("host", "invalid-host")
            .param("redirect", "https://google.com/path")
            .cookie(new Cookie("authToken", "validToken")))
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attribute("errorMessage", "Invalid token"));
  }
}
