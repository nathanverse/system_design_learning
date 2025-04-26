package com.enjoy.ds.ratelimiter.utils;

import java.util.List;
import java.util.Objects;

import lombok.NonNull;
import org.springframework.security.core.parameters.P;

public class SearchingUtil {
  public static int searchMaximumElementLowerThan(
      @NonNull List<Long> timestamps, @NonNull Long timestamp) {
    if (timestamps.isEmpty()) {
      throw new IllegalArgumentException("Can not search in empty list");
    }

    int left = 0;
    int right = timestamps.size();
    int res = -1;

    while (left < right) {
      int mid = left + (right - left) / 2;

      if(timestamps.get(mid) >= timestamp){
        right = mid;
      } else if(timestamps.get(mid) < timestamp) {
        res = mid;
        left = mid + 1;
      }
    }

    if(res != -1 && Objects.equals(timestamps.get(res), timestamp)){
      return -1;
    }

    return res;
  }
}
