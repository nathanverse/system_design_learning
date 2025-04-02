package com.enjoy.ds.ratelimiter.auth;

//import com.enjoy.ds.ratelimiter.model.User;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements ReactiveUserDetailsService {
    public Mono<UserDetails> findByUsername(String username){
        if ("admin".equals(username)) {
            return Mono.just(User.withUsername("admin")
                    .password("$2a$10$IJghHStG8tMvZnOvdblT7.hFnrnb98LEuNhCok0SpgnQ.A/PHD0eq") // "password" encoded
                    .roles("ADMIN")
                    .build());
        } else if ("user".equals(username)) {
            return Mono.just(User.withUsername("user")
                    .password("$2a$10$IJghHStG8tMvZnOvdblT7.hFnrnb98LEuNhCok0SpgnQ.A/PHD0eq") // "password" encoded
                    .roles("USER")
                    .build());
        }
        return Mono.empty();
    }
}
