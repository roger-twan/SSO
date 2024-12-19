package com.roger.sso.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.roger.sso.dto.UserInfoDto;
import com.roger.sso.entity.UserActivityLog;
import com.roger.sso.entity.UserAuthedHost;
import com.roger.sso.interceptor.AuthInterceptor;
import com.roger.sso.service.RedisService;
import com.roger.sso.service.UserService;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@WebMvcTest(RootController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RootControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private AuthInterceptor authInterceptor;

  @MockitoBean
  private RedisService redisService;

  @Test
  public void testGetHomePage() throws Exception {
    String authToken = "validAuthToken";
    UserInfoDto mockUserInfo = new UserInfoDto();
    mockUserInfo.setEmail("test@example.com");

    List<UserAuthedHost> mockAuthorizedHosts = new ArrayList<>();
    UserAuthedHost mockAuthorizedHost = new UserAuthedHost();
    mockAuthorizedHost.setId("testId");
    mockAuthorizedHost.setHost("www.example.com");
    mockAuthorizedHost.setTimestamp(System.currentTimeMillis());
    mockAuthorizedHosts.add(mockAuthorizedHost);

    mockUserInfo.setAuthorizedHosts(mockAuthorizedHosts);

    List<UserActivityLog> mockUserActivityLogs = new ArrayList<>();
    UserActivityLog mockActivityLog = new UserActivityLog();
    mockActivityLog.setId("testId1");
    mockActivityLog.setUserId("testUserId1");
    mockActivityLog.setType(1);
    mockActivityLog.setTimestamp(System.currentTimeMillis());
    mockUserActivityLogs.add(mockActivityLog);

    mockUserInfo.setActivityLogs(mockUserActivityLogs);

    when(userService.getUserInfo(authToken)).thenReturn(mockUserInfo);

    when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);

    String content = mockMvc.perform(get("/")
        .cookie(new Cookie("authToken", authToken)))
        .andExpect(status().isOk())
        .andExpect(view().name("home"))
        .andExpect(model().attribute("userInfo", mockUserInfo))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Document document = Jsoup.parse(content);

    assertThat(document.title()).isEqualTo("Home | SSO");
  }
}
