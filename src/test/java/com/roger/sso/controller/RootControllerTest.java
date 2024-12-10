package com.roger.sso.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.roger.sso.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@WebMvcTest(RootController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RootControllerTest {
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
  public void testGetHomePage() throws Exception {
    String content = mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("home"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Document document = Jsoup.parse(content);
    assertThat(document.title()).isEqualTo("Home | SSO");
  }
}
