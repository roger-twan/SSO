package com.roger.sso.service;

import com.roger.sso.dto.SignUpDto;
import com.roger.sso.entity.User;
import com.roger.sso.enums.VerificationError;
import com.roger.sso.exception.VerificationException;
import com.roger.sso.repository.UserRepository;
import com.roger.sso.util.PasswordUtil;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

@SpringBootTest
public class UserServiceTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenService tokenService;

  @Mock
  private EmailService emailService;

  @Mock
  private RedisService redisService;

  @Mock
  private PasswordUtil passwordUtil;

  @InjectMocks
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
    verify(redisService).saveRedis("verify:" + signUpDto.getEmail(), mockToken, 60 * 5);
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

    verify(userRepository).save(argThat(user ->
        user.getEmail().equals(signUpDto.getEmail()) &&
        user.getPassword().equals(encodedPassed) &&
        user.getStatus() == 0 &&
        user.getId() != null));
    verify(redisService).saveRedis("verify:" + signUpDto.getEmail(), mockToken, 60 * 5);
    verify(emailService).sendActivationEmail(signUpDto.getEmail(), mockToken);
  }
  
  @Test
  public void testVerifyEmailWithUserNotFound() {
    String token = "userNotFoundToken";
    String email = "test@example.com";
    Claims claims = mock(Claims.class);

    when(claims.getSubject()).thenReturn(email);
    when(tokenService.parseToken(token)).thenReturn(claims);
    when(redisService.getRedis("verify:" + email)).thenReturn(null);
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
    when(redisService.getRedis("verify:" + email)).thenReturn(null);
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
    when(redisService.getRedis("verify:" + email)).thenReturn(null);
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
    when(redisService.getRedis("verify:" + email)).thenReturn("differentToken");
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
    when(redisService.getRedis("verify:" + email)).thenReturn(token);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    userService.verifyEmail(token);

    assertEquals(1, existingUser.getStatus());
    verify(userRepository).save(existingUser);
  }
}
