package com.enjoy.ds.ratelimiter.core.model;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class RateLimiterRuleService {

  private final Map<String, APIRule> rules =
      Map.of("post_a_post", new APIRule("post_a_post", 2, TimeUnit.SECONDS.toMillis(1)));

  public Mono<APIRule> getRule(String apiName) {
    return Mono.just(rules.getOrDefault(apiName, null));
  }
}
