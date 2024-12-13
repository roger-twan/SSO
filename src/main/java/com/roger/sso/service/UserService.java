package com.roger.sso.service;

import com.roger.sso.dto.SignUpDto;
import com.roger.sso.entity.User;
import com.roger.sso.enums.VerificationError;
import com.roger.sso.exception.VerificationException;
import com.roger.sso.repository.UserRepository;
import com.roger.sso.util.PasswordUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;

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
    redisService.saveRedis("verify:" + email, token, 60 * 5);
    emailService.sendActivationEmail(email, token);
  }

  public void verifyEmail(String token) {
    String email = tokenService.parseToken(token).getSubject();
    String redisToken = redisService.getRedis("verify:" + email);
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

  @Transactional
  public void deleteUserByEmail(String email) {
    userRepository.deleteByEmail(email);
  }
}
