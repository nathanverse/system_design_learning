package com.enjoy.ds.ratelimiter;

import com.enjoy.ds.ratelimiter.model.Post;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class PostRepository {
  public Mono<Post> findPost(String id) {
    return Mono.just(new Post(id, "hello"));
  }
}
