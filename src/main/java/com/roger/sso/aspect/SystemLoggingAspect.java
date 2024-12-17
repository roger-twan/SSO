package com.roger.sso.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SystemLoggingAspect {
  private static final Logger logger = LoggerFactory.getLogger(SystemLoggingAspect.class);

  @Before("execution(* com.roger.sso..*(..))")
  public void logBefore(JoinPoint joinPoint) {
    String methodName = joinPoint.getSignature().getName();
    logger.info("Entering method: " + methodName);
  }

  @After("execution(* com.roger.sso..*(..))")
  public void logAfter(JoinPoint joinPoint) {
    String methodName = joinPoint.getSignature().getName();
    logger.info("Exiting method: " + methodName);
  }

  @AfterReturning(pointcut = "execution(* com.roger.sso..*(..))", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    String methodName = joinPoint.getSignature().getName();
    logger.info("Method: " + methodName + " returned with value: " + result);
  }

  @AfterThrowing(pointcut = "execution(* com.roger.sso..*(..))", throwing = "exception")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
    String methodName = joinPoint.getSignature().getName();
    logger.error("Method: " + methodName + " threw exception: " + exception.getMessage());
  }
}
