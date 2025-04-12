package com.enjoy.ds.ratelimiter.config;

import com.enjoy.ds.ratelimiter.cache.RedisService;
import com.enjoy.ds.ratelimiter.core.model.DistributedSlidingWindowRateLimiterStorage;
import com.enjoy.ds.ratelimiter.core.model.SlidingWindowRateLimiterStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
  @Bean
  public RedisClient redisClient() {
    return RedisClient.create("redis://localhost:6379");
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public SlidingWindowRateLimiterStorage slidingWindowRateLimiterStorage(
      RedisService redisService) {
    return new DistributedSlidingWindowRateLimiterStorage(redisService);
  }
}
