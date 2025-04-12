package com.enjoy.ds.ratelimiter.core.model;

import com.enjoy.ds.ratelimiter.cache.RedisService;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

public class DistributedSlidingWindowRateLimiterStorage implements SlidingWindowRateLimiterStorage {
  private static final AtomicInteger counter = new AtomicInteger(0);
  private final RedisService redisService;
  private static final String LUA_SCRIPT =
      """
            local key = KEYS[1]
            local now_ms = tonumber(ARGV[1])
            local window_ms = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            local req_id = ARGV[4]

            -- Remove requests older than the window (in milliseconds)
            redis.call('ZREMRANGEBYSCORE', key, 0, now_ms - window_ms)

            -- Get the current number of requests in the window
            local count = redis.call('ZCARD', key)

            if count < limit then
                -- Add the current request with the current timestamp (in milliseconds)
                redis.call('ZADD', key, now_ms, req_id)

                -- Convert window from milliseconds to seconds before calling EXPIRE
                local window_sec = math.ceil(window_ms / 1000)
                redis.call('EXPIRE', key, window_sec)
                return 1
            else
                return 0
            end

            """;

  @Autowired
  public DistributedSlidingWindowRateLimiterStorage(RedisService redisService) {
    this.redisService = redisService;
  }

  @Override
  public Mono<Boolean> putTimeStamp(String key, Long reqTimeStamp, Long window, Integer limit) {
    return redisService
        .eval(
            LUA_SCRIPT,
            new String[] {key},
            String.valueOf(reqTimeStamp),
            String.valueOf(window),
            String.valueOf(limit),
            String.valueOf(UUID.randomUUID().toString()),
            String.valueOf(counter.getAndIncrement()))
        .map(result -> (Boolean) result);
  }
}
