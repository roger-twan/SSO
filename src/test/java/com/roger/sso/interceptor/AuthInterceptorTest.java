package com.roger.sso.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.roger.sso.service.RedisService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SpringBootTest
public class AuthInterceptorTest {
  @Mock
  private RedisService redisService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private AuthInterceptor authInterceptor;

  @Test
  public void testPreHandleWithValidToken() throws Exception {
    Cookie[] cookies = { new Cookie("authToken", "validToken") };
    when(request.getCookies()).thenReturn(cookies);
    when(redisService.getAuthTokenRedis("validToken")).thenReturn("validToken");

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertTrue(result);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  public void testPreHandleWithInvalidToken() throws Exception {
    Cookie[] cookies = { new Cookie("authToken", "invalidToken") };
    when(request.getCookies()).thenReturn(cookies);
    when(redisService.getAuthTokenRedis("invalidToken")).thenReturn(null);

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertFalse(result);
    verify(response).sendRedirect("/signin");
  }

  @Test
  public void testPreHandleWithNoAuthTokenCookie() throws Exception {
    when(request.getCookies()).thenReturn(null);

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertFalse(result);
    verify(response).sendRedirect("/signin");
  }

  @Test
  public void testPreHandleWithEmptyAuthToken() throws Exception {
    Cookie[] cookies = { new Cookie("authToken", "") };
    when(request.getCookies()).thenReturn(cookies);

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertFalse(result);
    verify(response).sendRedirect("/signin");
  }
}
