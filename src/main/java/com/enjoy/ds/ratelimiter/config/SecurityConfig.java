package com.enjoy.ds.ratelimiter.config;

import com.enjoy.ds.ratelimiter.auth.JwtAuthenticationConverter;
import com.enjoy.ds.ratelimiter.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final ReactiveUserDetailsService userDetailsService;
  private final JwtTokenProvider tokenProvider;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ReactiveAuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
    UserDetailsRepositoryReactiveAuthenticationManager manager =
        new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    manager.setPasswordEncoder(passwordEncoder);
    return manager;
  }

  private ReactiveAuthenticationManager jwtAuthenticationManager() {
    // Skip password check for JWT
    return Mono::just;
  }

  // We make this a private call, not bean, because provide this filter
  // as bean may cause it to be called twice, once by Spring Boot embedded
  // container, once by Spring Security.
  // See
  // https://docs.spring.io/spring-security/reference/servlet/architecture.html#_declaring_your_filter_as_a_bean
  private AuthenticationWebFilter authenticationWebFilter(
      ServerAuthenticationConverter jwtAuthenticationConverter) {
    AuthenticationWebFilter filter = new AuthenticationWebFilter(jwtAuthenticationManager());
    filter.setServerAuthenticationConverter(jwtAuthenticationConverter);
    return filter;
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers(HttpMethod.POST, "/api/auth/login")
                    .permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/auth/register")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .addFilterAt(
            authenticationWebFilter(jwtAuthenticationConverter()),
            SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
  }

  private ServerAuthenticationConverter jwtAuthenticationConverter() {
    return new JwtAuthenticationConverter(tokenProvider);
  }
}
