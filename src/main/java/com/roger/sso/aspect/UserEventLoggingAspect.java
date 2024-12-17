package com.roger.sso.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserEventLoggingAspect {

  @Pointcut("execution(* com.example.service.*.*(..))")
  public void serviceMethods() {
  }

  // Before Advice: Run before methods in the pointcut
  @Before("serviceMethods()")
  public void logBefore() {
    System.out.println("Logging before method execution...");
  }

  // After Returning Advice: Run after a successful execution
  @AfterReturning("serviceMethods()")
  public void logAfterReturning() {
    System.out.println("Logging after method successfully executed...");
  }

  // After Throwing Advice: Run if the method throws an exception
  @AfterThrowing("serviceMethods()")
  public void logAfterThrowing() {
    System.out.println("Logging after method throws exception...");
  }
}
