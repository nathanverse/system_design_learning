package com.enjoy.ds.ratelimiter.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@Value
public class APIRule {
    public String api;
    public int limit;
    public TimeUnit unit;
}
