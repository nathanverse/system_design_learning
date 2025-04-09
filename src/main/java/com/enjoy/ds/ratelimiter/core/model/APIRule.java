package com.enjoy.ds.ratelimiter.core.model;

import lombok.Data;
import lombok.Value;

@Data
@Value
public class APIRule {
  public String api;
  public int limit;
  public long millisecond;
}
