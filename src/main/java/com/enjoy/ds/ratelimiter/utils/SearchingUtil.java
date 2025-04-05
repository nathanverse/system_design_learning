package com.enjoy.ds.ratelimiter.utils;

import java.util.List;
import lombok.NonNull;

public class SearchingUtil {
  public static int searchMaximumElementLowerThan(
      @NonNull List<Long> timestamps, @NonNull Long timestamp) {
    if (timestamps.isEmpty()) {
      throw new IllegalArgumentException("Can not search in empty list");
    }

    if (timestamps.get(0) >= timestamp) {
      return -1;
    }

    int left = 0;
    int right = timestamps.size() - 1;
    int mid = left + (right - left) / 2;

    while (left <= right) {
      if ((timestamps.get(mid) < timestamp
          && (mid + 1 >= timestamps.size() || timestamps.get(mid + 1) >= timestamp))) {
        return mid;
      }

      if ((timestamps.get(mid) >= timestamp)) {
        right = mid - 1;
      } else {
        left = mid + 1;
      }

      mid = left + (right - left) / 2;
    }

    return -1;
  }
}
