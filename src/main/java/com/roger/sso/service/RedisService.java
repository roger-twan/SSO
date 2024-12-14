package com.roger.sso.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisService {
  private JedisPool jedisPool;
  
  @Value("${spring.redis.host}")
  private String host;

  @Value("${spring.redis.port}")
  private int port;

  @Value("${spring.redis.password}")
  private String password;

  @PostConstruct
  public void init() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(20);
    poolConfig.setMaxIdle(10);
    poolConfig.setMinIdle(5);
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);

    this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
  }

  public void saveRedis(String key, String value, long expirationTime) {
    // the jedis.close() is not explicitly called because the try-with-resources statement is used, which automatically handles the closing of resources, including returning the Jedis instance to the pool.
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.setex(key, expirationTime, value);
    }
  }

  public String getRedis(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.get(key);
    }
  }

  public void deleteRedis(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.del(key);
    }
  }

  public void shutdown() {
    if (jedisPool != null) {
      jedisPool.close();
    }
  }
}
