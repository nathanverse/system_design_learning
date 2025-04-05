package com.enjoy.ds.ratelimiter.auth;


import com.enjoy.ds.ratelimiter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements ReactiveUserDetailsService {
  private final UserService userService;

  public Mono<UserDetails> findByUsername(String username) {
    return userService
        .findByUserName(username)
        .map(
            user ->
                User.withUsername(
                        user.getId().toString()) // A little hack to include user id in jwt
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build());
  }
}
