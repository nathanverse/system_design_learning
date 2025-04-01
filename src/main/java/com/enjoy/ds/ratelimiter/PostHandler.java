package com.enjoy.ds.ratelimiter;

import com.enjoy.ds.ratelimiter.model.Post;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/posts")
public class PostHandler {
    private final PostRepository postRepository;

    public PostHandler(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Post>> getPost(@PathVariable String id) {
        return postRepository.findPost(id).map(
                post -> ResponseEntity.ok(post)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
