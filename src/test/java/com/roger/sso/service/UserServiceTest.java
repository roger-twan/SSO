package com.roger.sso.service;

import com.roger.sso.dto.SignInReqDto;
import com.roger.sso.dto.SignInResDto;
import com.roger.sso.dto.SignUpDto;
import com.roger.sso.entity.User;
import com.roger.sso.entity.UserAuthedHost;
import com.roger.sso.enums.VerificationError;
import com.roger.sso.exception.VerificationException;
import com.roger.sso.repository.UserAuthedHostRepository;
import com.roger.sso.repository.UserRepository;
import com.roger.sso.util.PasswordUtil;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

@SpringBootTest
public class UserServiceTest {
  @Value("${spring.token.expiration.verification.minutes}")
  private int verificationExpirationMinutes;

  @Value("${spring.token.expiration.auth.days}")
  private int authExpirationDays;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  private UserAuthedHostRepository userAuthedHostRepository;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private EmailService emailService;

  @MockitoBean
  private RedisService redisService;

  @MockitoBean
  private PasswordUtil passwordUtil;

  @Autowired
  private UserService userService;

  @Test
  public void testHandleSignUpWithSignedUpEmail() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("test@example.com");
    signUpDto.setPassword("Password123");

    User existingUser = new User();
    existingUser.setEmail(signUpDto.getEmail());
    existingUser.setStatus(1);

    doReturn(Optional.of(existingUser)).when(userRepository).findByEmail(signUpDto.getEmail());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      userService.handleSignUp(signUpDto);
    });

    assertEquals("Email already has been signed up.", exception.getMessage());
    verifyNoInteractions(tokenService, emailService, redisService);
  }

  @Test
  public void testHandleSignUpWithExistingUnverifiedUser() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("test@example.com");
    signUpDto.setPassword("password123");

    User existingUser = mock(User.class);
    existingUser.setEmail(signUpDto.getEmail());
    existingUser.setStatus(0);

    String encodedPassed = "encodedPassword";
    String mockToken = "mockToken";

    when(userRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.of(existingUser));
    when(passwordUtil.encode(signUpDto.getPassword())).thenReturn(encodedPassed);
    when(tokenService.generateToken(signUpDto.getEmail())).thenReturn(mockToken);

    userService.handleSignUp(signUpDto);
    verify(existingUser).setPassword(encodedPassed);
    verify(userRepository).save(existingUser);
    verify(redisService).saveVerifyTokenRedis(signUpDto.getEmail(), mockToken, 60 * verificationExpirationMinutes);
    verify(emailService).sendActivationEmail(signUpDto.getEmail(), mockToken);
  }

  @Test
  public void testHandleSignUpWithNewUser() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setEmail("newuser@example.com");
    signUpDto.setPassword("password123");

    String encodedPassed = "encodedPassword";
    String mockToken = "mockToken";

    when(userRepository.findByEmail(signUpDto.getEmail())).thenReturn(Optional.empty());
    when(passwordUtil.encode(signUpDto.getPassword())).thenReturn(encodedPassed);
    when(tokenService.generateToken(signUpDto.getEmail())).thenReturn(mockToken);

    userService.handleSignUp(signUpDto);

    verify(userRepository).save(argThat(user -> user.getEmail().equals(signUpDto.getEmail()) &&
        user.getPassword().equals(encodedPassed) &&
        user.getStatus() == 0 &&
        user.getId() != null));
    verify(redisService).saveVerifyTokenRedis(signUpDto.getEmail(), mockToken, 60 * verificationExpirationMinutes);
    verify(emailService).sendActivationEmail(signUpDto.getEmail(), mockToken);
  }

  @Test
  public void testVerifyEmailWithUserNotFound() {
    String token = "userNotFoundToken";
    String email = "test@example.com";
    Claims claims = mock(Claims.class);

    when(claims.getSubject()).thenReturn(email);
    when(tokenService.parseToken(token)).thenReturn(claims);
    when(redisService.getVerifyTokenRedis(email)).thenReturn(null);
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    VerificationException exception = assertThrows(VerificationException.class, () -> {
      userService.verifyEmail(token);
    });

    assertEquals(VerificationError.USER_NOT_FOUND, exception.getError());
  }

  @Test
  public void testVerifyEmailWithAlreadyVerified() {
    String token = "alreadyVerifiedToken";
    String email = "test@example.com";

    User existingUser = new User();
    existingUser.setStatus(1);

    Claims claims = mock(Claims.class);

    when(claims.getSubject()).thenReturn(email);
    when(tokenService.parseToken(token)).thenReturn(claims);
    when(redisService.getVerifyTokenRedis(email)).thenReturn(null);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    VerificationException exception = assertThrows(VerificationException.class, () -> {
      userService.verifyEmail(token);
    });

    assertEquals(VerificationError.ALREADY_VERIFIED, exception.getError());
  }

  @Test
  public void testVerifyEmailWithTokenExpired() {
    String token = "expiredToken";
    String email = "test@example.com";

    User existingUser = new User();
    existingUser.setStatus(0);

    Claims claims = mock(Claims.class);

    when(claims.getSubject()).thenReturn(email);
    when(tokenService.parseToken(token)).thenReturn(claims);
    when(redisService.getVerifyTokenRedis(email)).thenReturn(null);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    VerificationException exception = assertThrows(VerificationException.class, () -> {
      userService.verifyEmail(token);
    });

    assertEquals(VerificationError.TOKEN_EXPIRED, exception.getError());
  }

  @Test
  public void testVerifyEmailWithInvalidToken() {
    String token = "invalidToken";
    String email = "test@example.com";

    User existingUser = new User();
    existingUser.setStatus(0);

    Claims claims = mock(Claims.class);

    when(claims.getSubject()).thenReturn(email);
    when(tokenService.parseToken(token)).thenReturn(claims);
    when(redisService.getVerifyTokenRedis(email)).thenReturn("differentToken");
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    VerificationException exception = assertThrows(VerificationException.class, () -> {
      userService.verifyEmail(token);
    });

    assertEquals(VerificationError.INVALID_TOKEN, exception.getError());
  }

  @Test
  public void testVerifyEmailWithSuccess() {
    String token = "validToken";
    String email = "test@example.com";

    User existingUser = new User();
    existingUser.setStatus(0);

    Claims claims = mock(Claims.class);

    when(claims.getSubject()).thenReturn(email);
    when(tokenService.parseToken(token)).thenReturn(claims);
    when(redisService.getVerifyTokenRedis(email)).thenReturn(token);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    userService.verifyEmail(token);

    assertEquals(1, existingUser.getStatus());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void testHandleSignInWithSuccess() {
    String email = "test@example.com";
    String password = "password123";
    String hashedPassword = "hashedPassword";
    String token = "validToken";

    SignInReqDto signInDto = new SignInReqDto();
    signInDto.setEmail("test@example.com");
    signInDto.setPassword("password123");

    User user = new User();
    user.setEmail(email);
    user.setPassword(hashedPassword);
    user.setStatus(1);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordUtil.matches(password, hashedPassword)).thenReturn(true);
    when(tokenService.generateToken(email)).thenReturn(token);

    SignInResDto signInRes = userService.handleSignIn(signInDto);

    assertEquals(token, signInRes.getToken());
    assertEquals(authExpirationDays, signInRes.getAuthExpirationDays());

    verify(redisService).saveAuthTokenRedis(token, token, 60 * 60 * 24 * authExpirationDays);
  }

  @Test
  public void testHandleSignInWithEmailNotFound() {
    String email = "test@example.com";
    String password = "password123";

    SignInReqDto signInDto = new SignInReqDto();
    signInDto.setEmail(email);
    signInDto.setPassword(password);

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      userService.handleSignIn(signInDto);
    });

    assertEquals("Email or password is incorrect.", exception.getMessage());
    verifyNoInteractions(passwordUtil, tokenService, redisService);
  }

  @Test
  public void testHandleSignInWithPasswordMismatch() {
    String email = "test@example.com";
    String hashedPassword = "hashedPassword123";
    String wrongPassword = "wrongPassword123";

    SignInReqDto signInDto = new SignInReqDto();
    signInDto.setEmail(email);
    signInDto.setPassword(wrongPassword);

    User user = new User();
    user.setEmail(email);
    user.setPassword(hashedPassword);
    user.setStatus(1);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordUtil.matches(wrongPassword, hashedPassword)).thenReturn(false);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      userService.handleSignIn(signInDto);
    });

    assertEquals("Email or password is incorrect.", exception.getMessage());
    verifyNoInteractions(tokenService, redisService);
  }

  @Test
  public void testHandleSignInWithUnverifiedEmail() {
    String email = "test@example.com";
    String password = "password123";
    String hashedPassword = "hashedPassword123";

    SignInReqDto signInDto = new SignInReqDto();
    signInDto.setEmail(email);
    signInDto.setPassword(password);

    User user = new User();
    user.setEmail(email);
    user.setPassword(hashedPassword);
    user.setStatus(0);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordUtil.matches(password, hashedPassword)).thenReturn(true);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      userService.handleSignIn(signInDto);
    });

    assertEquals("Please verify your email first.", exception.getMessage());
    verifyNoInteractions(tokenService, redisService);
  }

  @Test
  public void verifyAuthorizedWithSuccess() {
    String token = "validToken";
    String host = "example.com";
    String userId = "user123";

    Claims claims = Mockito.mock(Claims.class);
    when(claims.get("userId")).thenReturn(userId);

    when(redisService.getAuthTokenRedis(token)).thenReturn(token);
    when(tokenService.parseToken(token)).thenReturn(claims);
    when(userAuthedHostRepository.findUserAuthedHost(userId, host)).thenReturn(Optional.of(new UserAuthedHost()));

    boolean result = userService.verifyAuthorized(token, host);

    assertTrue(result);

    verify(redisService).getAuthTokenRedis(token);
    verify(tokenService).parseToken(token);
    verify(userAuthedHostRepository).findUserAuthedHost(userId, host);
  }

  @Test
  public void verifyAuthorizedWithInvalidToken() {
    String token = "invalidToken";
    when(redisService.getAuthTokenRedis(token)).thenReturn(null);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> userService.verifyAuthorized(token, "example.com"));

    assertEquals("Invalid token.", exception.getMessage());

    verify(redisService).getAuthTokenRedis(token);
    verifyNoInteractions(tokenService, userAuthedHostRepository);
  }

  @Test
  public void addAuthHostWithSuccess() {
    String token = "validToken";
    String host = "example.com";
    String userId = "user123";

    Claims claims = Mockito.mock(Claims.class);
    when(claims.get("userId")).thenReturn(userId);

    when(redisService.getAuthTokenRedis(token)).thenReturn(token);
    when(tokenService.parseToken(token)).thenReturn(claims);

    userService.addAuthHost(token, host);

    verify(redisService).getAuthTokenRedis(token);
    verify(tokenService).parseToken(token);
    verify(userAuthedHostRepository).save(
        argThat(userAuthedHost -> userAuthedHost.getUserId().equals(userId) && userAuthedHost.getHost().equals(host)));
  }

  @Test
  public void addAuthHostWithInvalidToken() {
    String token = "invalidToken";
    when(redisService.getAuthTokenRedis(token)).thenReturn(null);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> userService.addAuthHost(token, "example.com"));

    assertEquals("Invalid token.", exception.getMessage());

    verify(redisService).getAuthTokenRedis(token);
    verifyNoInteractions(tokenService, userAuthedHostRepository);
  }

  @Test
  public void testGetAuthStatusWithTokenFound() {
    String token = "validToken";
    String redisValue = "someTokenValue";
    when(redisService.getAuthTokenRedis(token)).thenReturn(redisValue);

    assertTrue(userService.getAuthStatus(token));
  }

  @Test
  public void testGetAuthStatusWithTokenNotFound() {
    String token = "invalidToken";
    when(redisService.getAuthTokenRedis(token)).thenReturn(null);

    assertFalse(userService.getAuthStatus(token));
  }
}
