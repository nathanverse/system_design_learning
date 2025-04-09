package com.enjoy.ds.ratelimiter.config;

import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
  @Bean
  public RedisClient redisClient(RedisClient redisClient) {
    return RedisClient.create("redis://localhost:6379");
  }
}
