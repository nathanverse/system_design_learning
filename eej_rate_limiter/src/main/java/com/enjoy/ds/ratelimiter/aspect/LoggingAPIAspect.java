package com.enjoy.ds.ratelimiter.aspect;

import com.enjoy.ds.ratelimiter.annotation.LoggableApi;
import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAPIAspect {
  private static final Logger logger = LoggerFactory.getLogger(LoggingAPIAspect.class);

  @Before("@annotation(loggableApi)")
  public void logBefore(JoinPoint pjp, LoggableApi loggableApi) {
    MethodSignature signature = (MethodSignature) pjp.getSignature();
    String methodName = signature.getMethod().getName();
    String className = pjp.getTarget().getClass().getName();

    int annotationNumber = loggableApi.number();
    String apiName =
        loggableApi.apiName().isEmpty() ? className + "." + methodName : loggableApi.apiName();

    Object[] args = pjp.getArgs();
    String params = Arrays.toString(args);

    // Log all the information
    logger.info(
        "Access api={}, method={}, number={}, params={}",
        apiName,
        methodName,
        annotationNumber,
        params);
  }
}
