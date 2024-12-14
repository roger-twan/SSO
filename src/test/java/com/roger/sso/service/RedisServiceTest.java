package com.roger.sso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisServiceTest {
  @Autowired
  private RedisService redisService;

  @Test
  public void testRedisFunctions() {
    String key = "testKey" + (int) (Math.random() * 1000);
    String value = "testValue";
    long expirationTime = 5;

    redisService.saveRedis(key, value, expirationTime);

    String result = redisService.getRedis(key);
    assertEquals(value, result);

    redisService.deleteRedis(key);
    assertEquals(null, redisService.getRedis(key));
  }
}
