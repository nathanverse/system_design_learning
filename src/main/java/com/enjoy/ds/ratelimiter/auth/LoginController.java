package com.enjoy.ds.ratelimiter.auth;

import com.enjoy.ds.ratelimiter.model.AuthResponse;
import com.enjoy.ds.ratelimiter.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {
  private final AuthenticationService authenticationService;
  private final JwtTokenProvider tokenProvider;
  private final ReactiveAuthenticationManager authenticationManager;
  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

  @PostMapping("/login")
  public Mono<ResponseEntity<AuthResponse>> login(@RequestBody User user) {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
    return authenticationManager
        .authenticate(authentication)
        .map(
            auth -> {
              String jwt = tokenProvider.generateToken(auth);
              return ResponseEntity.ok(new AuthResponse(jwt));
            })
        .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
  }
}
