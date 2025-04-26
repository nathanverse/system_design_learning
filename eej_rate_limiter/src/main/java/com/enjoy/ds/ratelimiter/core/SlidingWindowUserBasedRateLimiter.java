package com.enjoy.ds.ratelimiter.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SlidingWindowUserBasedRateLimiter implements UserBasedRateLimiter {

  private final RateLimiterRuleService rateLimiterRuleService;
  private final SlidingWindowRateLimiterStorage slidingWindowRecordMap;
  private static final Logger logger =
      LoggerFactory.getLogger(SlidingWindowUserBasedRateLimiter.class);

  @Autowired
  public SlidingWindowUserBasedRateLimiter(
      RateLimiterRuleService rateLimiterRuleService,
      SlidingWindowRateLimiterStorage slidingWindowRecordMap) {
    this.rateLimiterRuleService = rateLimiterRuleService;
    this.slidingWindowRecordMap = slidingWindowRecordMap;
  }

  @Override
  public Mono<Boolean> isAllowed(String userIdentity, String apiName) {
    return Mono.defer(
        () -> {
          String key = key(userIdentity, apiName);
          Long requestTimeStamp = System.currentTimeMillis();
          logger.info("Check: {}", key);
          if (key.isEmpty()) {
            throw new IllegalStateException("Key can not be empty");
          }

          return rateLimiterRuleService
              .getRule(apiName)
              .flatMap(
                  apiRule -> {
                    if (apiRule == null) {
                      return Mono.error(
                          new IllegalStateException(
                              "Can not retrieve rate limiter rule for api: " + apiName + "."));
                    }

                    return slidingWindowRecordMap.putTimeStamp(
                        key, requestTimeStamp, apiRule.getMillisecond(), apiRule.getLimit());
                  });
        });
  }

  private String key(String userIdentity, String apiName) {
    return apiName + ":" + userIdentity;
  }
}
