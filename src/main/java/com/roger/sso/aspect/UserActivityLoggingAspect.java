package com.roger.sso.aspect;

import org.aspectj.lang.annotation.Aspect;

import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.roger.sso.dto.SignInReqDto;
import com.roger.sso.entity.User;
import com.roger.sso.entity.UserActivityLog;
import com.roger.sso.enums.UserActivityType;
import com.roger.sso.repository.UserActivityLogRepository;
import com.roger.sso.repository.UserRepository;
import com.roger.sso.service.TokenService;

@Aspect
@Component
public class UserActivityLoggingAspect {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserActivityLogRepository userActivityLogRepository;

  @Autowired
  private TokenService tokenService;

  @After("execution(* com.roger.sso.service.UserService.handleSignIn(..))")
  public void logAfterSignIn(JoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();

    for (Object arg : args) {
      if (arg instanceof SignInReqDto) {
        SignInReqDto signInReqDto = (SignInReqDto) arg;
        String email = signInReqDto.getEmail();
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
          String id = user.get().getId();

          UserActivityLog userActivityLog = new UserActivityLog();
          userActivityLog.setId(UUID.randomUUID().toString());
          userActivityLog.setUserId(id);
          userActivityLog.setType(UserActivityType.SIGN_IN.getCode());
          userActivityLog.setTimestamp(System.currentTimeMillis());

          userActivityLogRepository.save(userActivityLog);
        }
      }
    }
  }

  @After("execution(* com.roger.sso.service.UserService.addAuthHost(..))")
  public void logAfterAddAuthHost(JoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    String token = (String) args[0];
    String userId = tokenService.parseToken(token).get("userId").toString();

    UserActivityLog userActivityLog = new UserActivityLog();
    userActivityLog.setId(UUID.randomUUID().toString());
    userActivityLog.setUserId(userId);
    userActivityLog.setType(UserActivityType.AUTHORIZED.getCode());
    userActivityLog.setTimestamp(System.currentTimeMillis());

    userActivityLogRepository.save(userActivityLog);
  }

  @After("execution(* com.roger.sso.service.UserService.signOut(..))")
  public void logAfterSignOut(JoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();

    for (Object arg : args) {
      if (arg instanceof String) {
        String token = (String) arg;
        String userId = tokenService.parseToken(token).get("userId").toString();

        UserActivityLog userActivityLog = new UserActivityLog();
        userActivityLog.setId(UUID.randomUUID().toString());
        userActivityLog.setUserId(userId);
        userActivityLog.setType(UserActivityType.SIGN_OUT.getCode());
        userActivityLog.setTimestamp(System.currentTimeMillis());

        userActivityLogRepository.save(userActivityLog);
      }
    }
  }
}
