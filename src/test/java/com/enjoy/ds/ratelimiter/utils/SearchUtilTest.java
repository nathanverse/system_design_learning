package com.enjoy.ds.ratelimiter.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchUtilTest {
  @Test
  void happyPath() {
    List<Long> timestamps = List.of(1L, 2L, 4L, 5L, 6L);

    assertThat(SearchingUtil.searchMaximumElementLowerThan(timestamps, 3L)).isEqualTo(1);
  }

  @Test
  void greaterArray() {
    List<Long> timestamps = List.of(2L, 4L, 5L, 6L);

    assertThat(SearchingUtil.searchMaximumElementLowerThan(timestamps, 1L)).isEqualTo(-1);
  }

  @Test
  void lowerArray() {
    List<Long> timestamps = List.of(2L, 4L, 5L, 6L);

    assertThat(SearchingUtil.searchMaximumElementLowerThan(timestamps, 7L)).isEqualTo(3);
  }

  @Test
  void equalElementsInArray() {
    List<Long> timestamps = List.of(2L, 4L, 4L, 4L, 5L, 6L);

    assertThat(SearchingUtil.searchMaximumElementLowerThan(timestamps, 4L)).isEqualTo(0);
  }

  @Test
  void testTargetEqualToTheFirstElement() {
    List<Long> timestamps = List.of(7L, 9L, 11L);
    Long timestamp = 7L;
    int result = SearchingUtil.searchMaximumElementLowerThan(timestamps, timestamp);
    Assertions.assertEquals(-1, result); // No element is strictly lower
  }
}
