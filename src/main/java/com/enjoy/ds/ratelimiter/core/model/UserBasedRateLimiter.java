package com.enjoy.ds.ratelimiter.core.model;

public interface UserBasedRateLimiter {
  public boolean passOrNot(String userIdentity, String apiName);
}
