package com.enjoy.ds.ratelimiter.core.model;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RateLimiterRuleService {

  private final Map<String, APIRule> rules =
      Map.of("post_a_post", new APIRule("post_a_post", 3, TimeUnit.MINUTES));

  public APIRule getRule(String apiName) {
    return rules.getOrDefault(apiName, null);
  }
}
