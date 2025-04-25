package com.enjoy.ds.ratelimiter.cache;

import reactor.core.publisher.Mono;

interface CacheService {
  Mono<String> get(String key);

  Mono<String> set(String key, String value);
}
