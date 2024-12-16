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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
  @Value("${spring.token.expiration.verification.minutes}")
  private int verificationExpirationMinutes;

  @Value("${spring.token.expiration.auth.days}")
  private int authExpirationDays;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserAuthedHostRepository userAuthedHostRepository;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private EmailService emailService;

  @Autowired
  private RedisService redisService;

  @Autowired
  private PasswordUtil passwordUtil;

  public void handleSignUp(SignUpDto signUpDto) {
    String email = signUpDto.getEmail().toLowerCase().trim();
    Optional<User> optionalUser = userRepository.findByEmail(email);

    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      if (user.getStatus() == 1) {
        throw new IllegalArgumentException("Email already has been signed up.");
      } else if (user.getStatus() == 0) {
        user.setPassword(passwordUtil.encode(signUpDto.getPassword()));
        userRepository.save(user);
      }
    } else {
      User newUser = new User();
      String password = signUpDto.getPassword();
      newUser.setId(UUID.randomUUID().toString());
      newUser.setEmail(email);
      newUser.setPassword(passwordUtil.encode(password));
      newUser.setStatus(0);
      userRepository.save(newUser);
    }

    String token = tokenService.generateToken(email);
    redisService.saveVerifyTokenRedis(email, token, 60 * verificationExpirationMinutes);
    emailService.sendActivationEmail(email, token);
  }

  public void verifyEmail(String token) {
    String email = tokenService.parseToken(token).getSubject();
    String redisToken = redisService.getVerifyTokenRedis(email);
    Optional<User> OptionalUser = userRepository.findByEmail(email);

    if (redisToken == null) {
      if (OptionalUser.isEmpty()) {
        throw new VerificationException(VerificationError.USER_NOT_FOUND);
      } else if (OptionalUser.get().getStatus() == 1) {
        throw new VerificationException(VerificationError.ALREADY_VERIFIED);
      } else {
        throw new VerificationException(VerificationError.TOKEN_EXPIRED);
      }
    }

    if (redisToken.equals(token)) {
      User user = OptionalUser.get();
      user.setStatus(1);
      userRepository.save(user);
    } else {
      throw new VerificationException(VerificationError.INVALID_TOKEN);
    }
  }

  public SignInResDto handleSignIn(SignInReqDto signInDto) {
    String email = signInDto.getEmail().toLowerCase().trim();
    Optional<User> optionalUser = userRepository.findByEmail(email);

    if (optionalUser.isEmpty()) {
      throw new IllegalArgumentException("Email or password is incorrect.");
    } else {
      User user = optionalUser.get();
      String password = user.getPassword();
      int status = user.getStatus();
      
      if (passwordUtil.matches(signInDto.getPassword(), password)) {
        if (status == 0) {
          throw new IllegalArgumentException("Please verify your email first.");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = tokenService.generateToken(email, claims);
        redisService.saveAuthTokenRedis(token, token, 60 * 60 * 24 * authExpirationDays);
        return new SignInResDto(token, authExpirationDays);
      } else {
        throw new IllegalArgumentException("Email or password is incorrect.");
      }
    }
  }

  public boolean verifyAuthorized(String token, String host) {
    String redisToken = redisService.getAuthTokenRedis(token);
    if (redisToken == null || !redisToken.equals(token)) {
      throw new IllegalArgumentException("Invalid token.");
    } else {
      String userId = tokenService.parseToken(token).get("userId").toString();
      Optional<UserAuthedHost> optionalUserAuthedHost = userAuthedHostRepository.findUserAuthedHost(userId, host);

      return optionalUserAuthedHost.isPresent();
    }
  }

  public void addAuthHost(String token, String host) {
    String redisToken = redisService.getAuthTokenRedis(token);

    if (redisToken == null || !redisToken.equals(token)) {
      throw new IllegalArgumentException("Invalid token.");
    }

    String userId = tokenService.parseToken(token).get("userId").toString();

    UserAuthedHost userAuthedHost = new UserAuthedHost();
    userAuthedHost.setId(UUID.randomUUID().toString());
    userAuthedHost.setUserId(userId);
    userAuthedHost.setHost(host);

    userAuthedHostRepository.save(userAuthedHost);
  }

  public boolean getAuthStatus(String token) {
    String redisToken = redisService.getAuthTokenRedis(token);

    return redisToken != null;
  }
}
