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
import com.roger.sso.entity.UserEventLog;
import com.roger.sso.enums.UserEventType;
import com.roger.sso.repository.UserEventLogRepository;
import com.roger.sso.repository.UserRepository;
import com.roger.sso.service.TokenService;

@Aspect
@Component
public class UserEventLoggingAspect {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserEventLogRepository userEventLogRepository;

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

          UserEventLog userEventLog = new UserEventLog();
          userEventLog.setId(UUID.randomUUID().toString());
          userEventLog.setUserId(id);
          userEventLog.setType(UserEventType.SIGN_IN.getCode());
          userEventLog.setTimestamp(System.currentTimeMillis() + "");

          userEventLogRepository.save(userEventLog);
        }
      }
    }
  }

  @After("execution(* com.roger.sso.service.UserService.addAuthHost(..))")
  public void logAfterAddAuthHost(JoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    String token = (String) args[0];
    String userId = tokenService.parseToken(token).get("userId").toString();

    UserEventLog userEventLog = new UserEventLog();
    userEventLog.setId(UUID.randomUUID().toString());
    userEventLog.setUserId(userId);
    userEventLog.setType(UserEventType.AUTHORIZED.getCode());
    userEventLog.setTimestamp(System.currentTimeMillis() + "");

    userEventLogRepository.save(userEventLog);
  }

  @After("execution(* com.roger.sso.service.UserService.signOut(..))")
  public void logAfterSignOut(JoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();

    for (Object arg : args) {
      if (arg instanceof String) {
        String token = (String) arg;
        String userId = tokenService.parseToken(token).get("userId").toString();

        UserEventLog userEventLog = new UserEventLog();
        userEventLog.setId(UUID.randomUUID().toString());
        userEventLog.setUserId(userId);
        userEventLog.setType(UserEventType.SIGN_OUT.getCode());
        userEventLog.setTimestamp(System.currentTimeMillis() + "");

        userEventLogRepository.save(userEventLog);
      }
    }
  }
}
