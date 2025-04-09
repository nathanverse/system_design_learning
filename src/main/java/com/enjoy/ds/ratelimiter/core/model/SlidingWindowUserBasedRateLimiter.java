package com.enjoy.ds.ratelimiter.core.model;

import static com.enjoy.ds.ratelimiter.utils.SearchingUtil.searchMaximumElementLowerThan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlidingWindowUserBasedRateLimiter implements UserBasedRateLimiter {

  private final RateLimiterRuleService rateLimiterRuleService;
  private final ConcurrentHashMap<String, List<Long>> slidingWindowRecordMap =
      new ConcurrentHashMap<>();

  @Autowired
  public SlidingWindowUserBasedRateLimiter(RateLimiterRuleService rateLimiterRuleService) {
    this.rateLimiterRuleService = rateLimiterRuleService;
  }

  @Override
  public boolean passOrNot(String userIdentity, String apiName) {
    String key = key(userIdentity, apiName);
    Long requestTimeStamp = System.currentTimeMillis();

    if (key.isEmpty()) {
      throw new IllegalStateException("Key can not be empty");
    }

    APIRule apiRule = rateLimiterRuleService.getRule(apiName);

    if (apiRule == null) {
      throw new IllegalStateException(
          "Can not retrieve rate limiter rule for api: " + apiName + ".");
    }

    int requestNum =
        slidingWindowRecordMap
            .compute(
                key,
                (k, timestamps) -> {
                  if (timestamps == null) {
                    return List.of(requestTimeStamp);
                  }



                  long outDatedPoint = requestTimeStamp - apiRule.getMillisecond();
                  int maximumOutdatedElementIndex =
                      searchMaximumElementLowerThan(timestamps, outDatedPoint);

                    if(timestamps.size() == 50){
                        System.out.println("dsa");
                    }

                  List<Long> timeStampsAfterClear;
                  if (maximumOutdatedElementIndex == -1) {
                    timeStampsAfterClear =
                        new ArrayList<>(timestamps.subList(0, timestamps.size()));
                  } else if (maximumOutdatedElementIndex != timestamps.size() - 1) {
                    timeStampsAfterClear =
                        new ArrayList<>(
                            timestamps.subList(maximumOutdatedElementIndex + 1, timestamps.size()));
                  } else {
                    timeStampsAfterClear =
                        new ArrayList<>(); // If maximumOutdatedElementIndex is the last element,
                    // the subList would be empty
                  }

                  timeStampsAfterClear.add(requestTimeStamp);

                  return timeStampsAfterClear;
                })
            .size();


    return requestNum <= apiRule.limit;
  }

  private String key(String userIdentity, String apiName) {
    return apiName + ":" + userIdentity;
  }
}
