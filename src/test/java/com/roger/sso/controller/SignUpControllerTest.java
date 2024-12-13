package com.roger.sso.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.roger.sso.dto.SignUpDto;
import com.roger.sso.enums.VerificationError;
import com.roger.sso.exception.VerificationException;
import com.roger.sso.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@WebMvcTest(SignUpController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SignUpControllerTest {
  @Autowired
  private MockMvc mockMvc;
  
  @MockitoBean
  private UserService userService;

  @Test
  public void testGetSignUpPage() throws Exception {
    String content = mockMvc.perform(get("/signup"))
        .andExpect(status().isOk())
        .andExpect(view().name("signUp"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Sign Up | SSO");
    assertThat(document.select("input[name=email]").size()).isEqualTo(1);
    assertThat(document.select("input[name=password]").size()).isEqualTo(1);
    assertThat(document.select("input[name=confirmPassword]").size()).isEqualTo(1);
    assertThat(document.select("button[type=submit]").size()).isEqualTo(1);
  }

  @Test
  public void testHandleSignUpWithValidationError() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/signup")
        .flashAttr("signUpDto", new SignUpDto()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.view().name("signUp"))
        .andExpect(MockMvcResultMatchers.model().attributeExists("signUpDto"));
  }

  @Test
  public void testHandleSignUpWithEmailAlreadySignedUp() throws Exception {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("test@example");
    signUpDto.setPassword("Password123");
    signUpDto.setConfirmPassword("Password123");

    doThrow(new IllegalArgumentException("Email already has been signed up.")).when(userService).handleSignUp(signUpDto);

    mockMvc.perform(MockMvcRequestBuilders.post("/signup")
        .flashAttr("signUpDto", signUpDto))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.view().name("signUp"))
        .andExpect(MockMvcResultMatchers.model().attributeExists("errorMessage"))
        .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Email already has been signed up."))
        .andReturn();

    verify(userService).handleSignUp(signUpDto);
  }

  @Test
  public void testHandleSignUpWithSuccess() throws Exception {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("test@example");
    signUpDto.setPassword("Password123");
    signUpDto.setConfirmPassword("Password123");
    
    doNothing().when(userService).handleSignUp(any(SignUpDto.class));

    String content = mockMvc.perform(MockMvcRequestBuilders.post("/signup")
        .flashAttr("signUpDto", signUpDto))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.view().name("emailSent"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Email Sent | SSO");
    assertThat(document.select("h2").first().text()).isEqualTo("Thanks for signing up");

    verify(userService).handleSignUp(signUpDto);
  }

  @Test
  public void testGetVerifyEmailWithSuccess() throws Exception {
    String token = "validToken";
    String content = mockMvc.perform(get("/signup/verify_email/{token}", token))
        .andExpect(status().isOk())
        .andExpect(view().name("verifyEmail"))
        .andExpect(MockMvcResultMatchers.model().attribute("result", "SUCCESS"))
        .andReturn()
        .getResponse()
        .getContentAsString();
    
    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Verify Email | SSO");
    assertThat(document.select("h2").first().text()).isEqualTo("Verification successful");

    verify(userService).verifyEmail(token);
  }

  @Test
  public void testGetVerifyEmailWithUserNotFound() throws Exception {
    String token = "userNotFoundToken";

    doThrow(new VerificationException(VerificationError.USER_NOT_FOUND)).when(userService).verifyEmail(token);

    String content = mockMvc.perform(get("/signup/verify_email/{token}", token))
        .andExpect(status().isOk())
        .andExpect(view().name("verifyEmail"))
        .andExpect(MockMvcResultMatchers.model().attribute("result", "USER_NOT_FOUND"))
        .andReturn()
        .getResponse()
        .getContentAsString();
    
    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Verify Email | SSO");
    assertThat(document.select("h2").first().text()).isEqualTo("Verification failed");

    verify(userService).verifyEmail(token);
  }

  @Test
  public void testGetVerifyEmailWithAlreadyVerified() throws Exception {
    String token = "alreadyVerifiedToken";

    doThrow(new VerificationException(VerificationError.ALREADY_VERIFIED)).when(userService).verifyEmail(token);

    String content = mockMvc.perform(get("/signup/verify_email/{token}", token))
        .andExpect(status().isOk())
        .andExpect(view().name("verifyEmail"))
        .andExpect(MockMvcResultMatchers.model().attribute("result", "ALREADY_VERIFIED"))
        .andReturn()
        .getResponse()
        .getContentAsString();
    
    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Verify Email | SSO");
    assertThat(document.select("h2").first().text()).isEqualTo("Verification successful");

    verify(userService).verifyEmail(token);
  }

  @Test
  public void testGetVerifyEmailWithInvalidToken() throws Exception {
    String token = "invalidToken";

    doThrow(new VerificationException(VerificationError.INVALID_TOKEN)).when(userService).verifyEmail(token);

    String content = mockMvc.perform(get("/signup/verify_email/{token}", token))
        .andExpect(status().isOk())
        .andExpect(view().name("verifyEmail"))
        .andExpect(MockMvcResultMatchers.model().attribute("result", "INVALID_TOKEN"))
        .andReturn()
        .getResponse()
        .getContentAsString();
    
    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Verify Email | SSO");
    assertThat(document.select("h2").first().text()).isEqualTo("Verification failed");

    verify(userService).verifyEmail(token);
  }

  @Test
  public void testGetVerifyEmailTokenExpired() throws Exception {
    String token = "expiredToken";

    doThrow(new VerificationException(VerificationError.TOKEN_EXPIRED)).when(userService).verifyEmail(token);

    String content = mockMvc.perform(get("/signup/verify_email/{token}", token))
        .andExpect(status().isOk())
        .andExpect(view().name("verifyEmail"))
        .andExpect(MockMvcResultMatchers.model().attribute("result", "TOKEN_EXPIRED"))
        .andReturn()
        .getResponse()
        .getContentAsString();
    
    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Verify Email | SSO");
    assertThat(document.select("h2").first().text()).isEqualTo("Verification failed");

    verify(userService).verifyEmail(token);
  }
}
