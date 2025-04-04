package com.enjoy.ds.ratelimiter;

import com.enjoy.ds.ratelimiter.model.User;

public interface UserBasedRateLimiter {
    boolean passOrNot(User user, String apiName);
}
