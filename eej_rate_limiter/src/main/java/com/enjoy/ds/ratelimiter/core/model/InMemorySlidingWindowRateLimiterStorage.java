package com.enjoy.ds.ratelimiter.core.model;

import static com.enjoy.ds.ratelimiter.utils.SearchingUtil.searchMaximumElementLowerThan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import reactor.core.publisher.Mono;

public class InMemorySlidingWindowRateLimiterStorage implements SlidingWindowRateLimiterStorage {
  private final ConcurrentHashMap<String, List<Long>> slidingWindowRecordMap =
      new ConcurrentHashMap<>();

  private ArrayList<Long> removeOutdatedTimeStamp(
      List<Long> timestamps, Long reqTimeStamp, Long window) {
    int maximumOutdatedElementIndex =
        searchMaximumElementLowerThan(timestamps, reqTimeStamp - window);

    if (maximumOutdatedElementIndex == -1) {
      return new ArrayList<>(timestamps.subList(0, timestamps.size()));
    } else if (maximumOutdatedElementIndex != timestamps.size() - 1) {
      return new ArrayList<>(
          timestamps.subList(maximumOutdatedElementIndex + 1, timestamps.size()));
    } else {
      return new ArrayList<>(); // If maximumOutdatedElementIndex is the last element,
    }
  }

  @Override
  public Mono<Boolean> putTimeStamp(String key, Long reqTimeStamp, Long window, Integer limit) {
    return Mono.defer(
        () -> {
          if (key.isEmpty()) {
            return Mono.error(new IllegalStateException("Key can not be empty"));
          }

          if (window <= 0) {
            return Mono.error(new IllegalStateException("Window can not be lower than 0"));
          }

          if (limit < 0) {
            return Mono.error(new IllegalStateException("Limit can not be lower than 0"));
          }

          if (limit == 0) {
            return Mono.just(false);
          }

          AtomicBoolean isPass = new AtomicBoolean(false);
          slidingWindowRecordMap.compute(
              key,
              (k, timestamps) -> {
                if (timestamps == null) {
                  isPass.set(true);
                  return List.of(reqTimeStamp);
                }

                List<Long> timeStampsAfterClear =
                    removeOutdatedTimeStamp(timestamps, reqTimeStamp, window);

                int size = timeStampsAfterClear.size();
                if (size < limit) {
                  timeStampsAfterClear.add(reqTimeStamp);
                  isPass.set(true);
                }

                return timeStampsAfterClear;
              });
          return Mono.just(isPass.get());
        });
  }
}
