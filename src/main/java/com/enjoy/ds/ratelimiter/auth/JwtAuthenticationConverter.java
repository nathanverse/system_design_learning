package com.enjoy.ds.ratelimiter.auth;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

  private final JwtTokenProvider tokenProvider;

  public JwtAuthenticationConverter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    return Mono.justOrEmpty(exchange)
        .flatMap(this::extractToken)
        .flatMap(
            token -> {
              String authToken = token.substring(7); // Remove "Bearer "
              return tokenProvider
                  .validateToken(authToken)
                  .filter(valid -> valid)
                  .flatMap(
                      valid -> {
                        String userId = tokenProvider.getUserId(authToken);
                        List<SimpleGrantedAuthority> roles =
                            tokenProvider.getAuthoritiesFromToken(authToken);
                        return Mono.just(
                            new UsernamePasswordAuthenticationToken(userId, null, roles));
                      });
            });
  }

  private Mono<String> extractToken(ServerWebExchange exchange) {
    return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
        .filter(authHeader -> authHeader.startsWith("Bearer "))
        .switchIfEmpty(Mono.empty());
  }

  private List<SimpleGrantedAuthority> extractAuthorities(String token) {
    // Implement logic to extract authorities from token
    // This would require parsing the JWT claims
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }
}
