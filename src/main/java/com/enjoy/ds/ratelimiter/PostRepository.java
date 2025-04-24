package com.enjoy.ds.ratelimiter;

import com.enjoy.ds.ratelimiter.cache.RedisService;
import com.enjoy.ds.ratelimiter.model.Post;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class PostRepository {
  private RedisService redisService;
  private ObjectMapper objectMapper;

  private static final Logger logger = LoggerFactory.getLogger(PostRepository.class);

  @Autowired
  public PostRepository(RedisService redisService, ObjectMapper objectMapper) {
    this.redisService = redisService;
    this.objectMapper = objectMapper;
  }

  protected String key(String id) {
    return "post:" + id;
  }

  public Mono<Post> findPost(String id) {
    Mono<Post> setPostRedisMono =
        Mono.defer(
            () -> {
              Post newPost = new Post(id, "hello");
              try {
                String newPostString = objectMapper.writeValueAsString(newPost);
                return redisService.set(key(id), newPostString).thenReturn(newPost);
              } catch (JsonProcessingException e) {
                System.err.println("Error serializing object: " + e.getMessage());
                return Mono.just(newPost);
              }
            });

    return redisService
        .get(key(id))
        .flatMap(
            value -> {
              if (value != null) {
                try {
                  return Mono.just(objectMapper.readValue(value, Post.class));
                } catch (IOException e) {
                  logger.error(
                      "Error deserializing object: {}, redirect to call main storage",
                      e.getMessage());
                }
              } else {
                logger.error("Something goes wrong with Redis Lettuce, it shouldn't produce null");
              }

              return Mono.empty();
            })
        .switchIfEmpty(setPostRedisMono)
        .onErrorResume(
            e -> {
              logger.error(
                  "Something goes wrong with Redis, fallback to pass all requests, error message: {}",
                  e.getMessage());
              return Mono.just(new Post(id, "hello"));
            }); // Fallback passing all requests
  }
}
