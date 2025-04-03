package com.enjoy.ds.ratelimiter.aspect;

import com.enjoy.ds.ratelimiter.annotation.LoggableApi;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.naming.AuthenticationException;
import java.util.Arrays;

@Aspect
@Component
public class ReactiveLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ReactiveLoggingAspect.class);

    @Around("@annotation(loggableApi)")
    public Mono<?> logApI(ProceedingJoinPoint joinPoint, LoggableApi loggableApi) {
        logger.warn("Around reactive method: ");
            return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                    String methodName = signature.getMethod().getName();
                    String className = joinPoint.getTarget().getClass().getName();

                    int annotationNumber = loggableApi.number();
                    String apiName = loggableApi.apiName().isEmpty() ? className + "." + methodName : loggableApi.apiName();

                    String username = auth.getName();
                    Object[] args = joinPoint.getArgs();
                    String params = Arrays.toString(args);


                    // Log all the information
                    logger.info("Access api={}, username={}, method={}, number={}, params={}", apiName, username, methodName, annotationNumber, params);

                    try {
                        return ((Mono<?>) joinPoint.proceed());
                    } catch (Throwable e) {
                        return Mono.error(e);
                    }
                }).switchIfEmpty(Mono.defer(() -> {
                        return Mono.error(AuthenticationException::new);
                }));
    }
}
