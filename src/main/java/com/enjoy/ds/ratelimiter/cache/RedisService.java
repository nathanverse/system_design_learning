package com.enjoy.ds.ratelimiter.cache;

import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RedisService implements CacheService {
  private final RedisReactiveCommands<String, String> reactiveCommands;

  @Autowired
  public RedisService(RedisClient redisClient) {
    this.reactiveCommands = redisClient.connect().reactive();
  }

  public Mono<String> get(String key) {
    return reactiveCommands.get(key);
  }

  public Mono<String> set(String key, String value) {
    return reactiveCommands.set(key, value);
  }

  public Mono<?> eval(String script, String[] keys, String... args) {
    Flux<Boolean> fluxResult =
        this.reactiveCommands.eval(script, ScriptOutputType.BOOLEAN, keys, args);
    return fluxResult.next();
  }

  public String flushAll() {
    return reactiveCommands.flushall().block();
  }
}
