package com.enjoy.ds.ratelimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "redis")
public record RedisConfig(@DefaultValue("localhost") String host, @DefaultValue("6379") int port) {}
