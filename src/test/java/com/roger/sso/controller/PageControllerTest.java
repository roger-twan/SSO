package com.roger.sso.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@WebMvcTest(PageController.class)
public class PageControllerTest {
  @Autowired
  private MockMvc mockMvc;

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
