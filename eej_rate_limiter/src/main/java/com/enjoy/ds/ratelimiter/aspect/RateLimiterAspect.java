package com.enjoy.ds.ratelimiter.aspect;

import com.enjoy.ds.ratelimiter.annotation.RateLimit;
import com.enjoy.ds.ratelimiter.core.UserBasedRateLimiter;
import javax.naming.AuthenticationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class RateLimiterAspect {
  private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);
  private final UserBasedRateLimiter rateLimiter;

  @Autowired
  public RateLimiterAspect(UserBasedRateLimiter userBasedRateLimiter) {
    this.rateLimiter = userBasedRateLimiter;
  }

  @Around("@annotation(rateLimit)")
  public Mono<?> processRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .flatMap(
            auth -> {
              String userIdString = auth.getName();
              String apiName = rateLimit.apiName();

              return this.rateLimiter.isAllowed(userIdString, apiName);
            })
        .flatMap(
            isAllowed -> {
              try {
                if (isAllowed) {
                  return ((Mono<?>) joinPoint.proceed());
                }

                return Mono.just(
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Rate limit exceeded. Please try again later."));
              } catch (Throwable e) {
                return Mono.error(e);
              }
            })
        .onErrorResume(
            e ->
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Something goes wrong, message: " + e.getMessage() + ".")))
        .switchIfEmpty(Mono.defer(() -> Mono.error(AuthenticationException::new)));
  }
}
