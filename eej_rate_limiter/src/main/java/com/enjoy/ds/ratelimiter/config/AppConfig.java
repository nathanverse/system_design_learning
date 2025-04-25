package com.enjoy.ds.ratelimiter.config;

import com.enjoy.ds.ratelimiter.cache.RedisService;
import com.enjoy.ds.ratelimiter.core.model.DistributedSlidingWindowRateLimiterStorage;
import com.enjoy.ds.ratelimiter.core.model.SlidingWindowRateLimiterStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(RedisConfig.class)
public class AppConfig {
  @Bean
  public RedisClient redisClient(RedisConfig redisConfig) {
    return RedisClient.create(
        new RedisURI(redisConfig.host(), redisConfig.port(), Duration.of(2000, ChronoUnit.MILLIS)));
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
