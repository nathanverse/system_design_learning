package com.enjoy.ds.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableApi {
    int number() default 0;  // The number parameter you requested
    String apiName() default ""; // Optional API name
}

