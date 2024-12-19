package com.roger.sso.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.roger.sso.dto.SignInReqDto;
import com.roger.sso.dto.SignInResDto;
import com.roger.sso.service.RedisService;
import com.roger.sso.service.UserService;

import jakarta.servlet.http.Cookie;

@WebMvcTest(SignInController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SignInControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private RedisService redisService;

  @Test
  public void testGetSignInPageWithAuthenticatedAndRedirect() throws Exception {
    String authToken = "validToken";
    String redirect = "https://test.com";

    doReturn(true).when(userService).verifyAuthToken(authToken);

    mockMvc.perform(get("/signin")
        .param("redirect", redirect)
        .cookie(new Cookie("authToken", authToken)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/auth?redirect=" + redirect));

    verify(userService).verifyAuthToken(authToken);
  }

  @Test
  public void testGetSignInPageWithAuthenticatedAndNoRedirect() throws Exception {
    String authToken = "validToken";

    doReturn(true).when(userService).verifyAuthToken(authToken);

    mockMvc.perform(get("/signin")
        .cookie(new Cookie("authToken", authToken)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));

    verify(userService).verifyAuthToken(authToken);
  }

  @Test
  public void testGetSignInPageWithNotAuthenticated() throws Exception {
    String authToken = "";
    String redirect = "https://test.com";

    String content = mockMvc.perform(get("/signin")
        .param("redirect", redirect)
        .cookie(new Cookie("authToken", authToken)))
        .andExpect(status().isOk())
        .andExpect(view().name("signIn"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Sign In | SSO");
    assertThat(document.select("input[name=email]").size()).isEqualTo(1);
    assertThat(document.select("input[name=password]").size()).isEqualTo(1);
    assertThat(document.select("input[name=redirect]").size()).isEqualTo(1);
    assertThat(document.select("button[type=submit]").size()).isEqualTo(1);
    assertThat(document.select("input[name=redirect]").attr("value")).isEqualTo(redirect);
  }

  @Test
  public void testGetSignInPageWithNotAuthenticatedAndNoRedirect() throws Exception {
    String content = mockMvc.perform(get("/signin"))
        .andExpect(status().isOk())
        .andExpect(view().name("signIn"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Sign In | SSO");
    assertThat(document.select("input[name=email]").size()).isEqualTo(1);
    assertThat(document.select("input[name=password]").size()).isEqualTo(1);
    assertThat(document.select("input[name=redirect]").size()).isEqualTo(1);
    assertThat(document.select("button[type=submit]").size()).isEqualTo(1);
    assertThat(document.select("input[name=redirect]").attr("value")).isEqualTo("");
  }

  @Test
  public void testHandleSignInWithValidationError() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/signin")
        .flashAttr("signInReqDto", new SignInReqDto()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.view().name("signIn"))
        .andExpect(MockMvcResultMatchers.model().attributeExists("signInReqDto"));
  }

  @Test
  public void testHandleSignInWithSuccessAndRedirect() throws Exception {
    String email = "test@example";
    String password = "Password123";
    String token = "validToken";
    String redirect = "https://test.com";
    int authExpirationDays = 7;

    SignInReqDto signInReqDto = new SignInReqDto();
    signInReqDto.setEmail(email);
    signInReqDto.setPassword(password);
    signInReqDto.setRedirect(redirect);

    SignInResDto signInResDto = new SignInResDto(token, authExpirationDays);

    doReturn(signInResDto).when(userService).handleSignIn(signInReqDto);

    mockMvc.perform(MockMvcRequestBuilders.post("/signin")
        .flashAttr("signInReqDto", signInReqDto))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/auth?redirect=" + redirect))
        .andExpect(cookie().exists("authToken"))
        .andExpect(cookie().value("authToken", token))
        .andExpect(cookie().httpOnly("authToken", true));
  }

  @Test
  public void testHandleSignInWithSuccessAndNoRedirect() throws Exception {
    String email = "test@example";
    String password = "Password123";
    String token = "validToken";
    int authExpirationDays = 7;

    SignInReqDto signInReqDto = new SignInReqDto();
    signInReqDto.setEmail(email);
    signInReqDto.setPassword(password);

    SignInResDto signInResDto = new SignInResDto(token, authExpirationDays);

    doReturn(signInResDto).when(userService).handleSignIn(signInReqDto);

    mockMvc.perform(MockMvcRequestBuilders.post("/signin")
        .flashAttr("signInReqDto", signInReqDto))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(cookie().exists("authToken"))
        .andExpect(cookie().value("authToken", token))
        .andExpect(cookie().httpOnly("authToken", true));
  }

  @Test
  public void testHandleSignInWithUserNotExists() throws Exception {
    String email = "test@example";
    String password = "Password123";

    SignInReqDto signInReqDto = new SignInReqDto();
    signInReqDto.setEmail(email);
    signInReqDto.setPassword(password);

    doThrow(new IllegalArgumentException("Email or password is incorrect.")).when(userService)
        .handleSignIn(signInReqDto);

    mockMvc.perform(MockMvcRequestBuilders.post("/signin")
        .flashAttr("signInReqDto", signInReqDto))
        .andExpect(status().isOk())
        .andExpect(view().name("signIn"))
        .andExpect(model().attributeExists("errorMessage"))
        .andExpect(model().attribute("errorMessage", "Email or password is incorrect."));
  }
}
