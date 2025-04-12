package com.enjoy.ds.ratelimiter.core.model;

import reactor.core.publisher.Mono;

public interface UserBasedRateLimiter {
  public Mono<Boolean> isAllowed(String userIdentity, String apiName);
}
