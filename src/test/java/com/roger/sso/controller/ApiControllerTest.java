
package com.roger.sso.controller;

import com.roger.sso.service.RedisService;
import com.roger.sso.service.UserService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ApiControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private RedisService redisService;

  @Test
  public void verifyAuthTokenWithSuccess() throws Exception {
    String token = "validToken";
    when(userService.verifyAuthToken(token)).thenReturn(true);

    mockMvc
      .perform(get("/api/verify_auth_token").param("token", token))
      .andExpect(status().isOk())
      .andExpect(content().string("true"));

    verify(userService).verifyAuthToken(token);
  }

  @Test
  public void verifyAuthTokenWithFailure() throws Exception {
    String token = "invalidToken";
    when(userService.verifyAuthToken(token)).thenReturn(false);

    mockMvc
      .perform(get("/api/verify_auth_token").param("token", token))
      .andExpect(status().isOk())
      .andExpect(content().string("false"));

    verify(userService).verifyAuthToken(token);
  }
}
