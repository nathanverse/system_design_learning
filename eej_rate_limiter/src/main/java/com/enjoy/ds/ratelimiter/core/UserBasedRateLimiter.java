package com.enjoy.ds.ratelimiter.core;

import reactor.core.publisher.Mono;

public interface UserBasedRateLimiter {
  public Mono<Boolean> isAllowed(String userIdentity, String apiName);
}
