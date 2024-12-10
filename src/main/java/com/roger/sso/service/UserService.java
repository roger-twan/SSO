package com.roger.sso.service;

import com.roger.sso.dto.SignUpDto;
import com.roger.sso.entity.User;
import com.roger.sso.repository.UserRepository;
import com.roger.sso.util.PasswordUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

  public void handleSignUp(SignUpDto signUpDto) {
    String email = signUpDto.getEmail().toLowerCase().trim();
    Optional<User> user = userRepository.findByEmail(email);

    if (user.isPresent()) {
      throw new IllegalArgumentException("Email already has been registered.");
    } else {
      User newUser = new User();
      String password = signUpDto.getPassword();
      newUser.setId(UUID.randomUUID().toString());
      newUser.setEmail(email);
      newUser.setPassword(PasswordUtil.encode(password));
      newUser.setStatus(0);
      userRepository.save(newUser);

      String token = tokenService.generateToken(email);
      redisService.saveRedis("verify:" + email, token, (long)(60 * 5));
      emailService.sendHtmlActivationEmail(email, token);
    }
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User createUser(User user) {
    return userRepository.save(user);
  }
}
