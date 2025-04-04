package com.enjoy.ds.ratelimiter.core.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RateLimiterRuleService {

    Map<String, APIRule> rules = Map.of(
           "post_a_post", new APIRule("post_a_post", 2, TimeUnit.SECONDS)
    );

    public RateLimiterRuleService(){

    }

    public APIRule getRule(String apiName){
        return rules.getOrDefault(apiName, null);
    }
}
