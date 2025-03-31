# Repository to learn system design
This repo contains apps built with the purpose of learning system design concepts.

## Rate limiter

---

### Goal
+ [ ] Implement sliding window algorithm on multiple rate limiter instances using Redis as 
memory tool and locking mechanism.
  + Ensure distributed timing.
  + Ensure Redis, middleware failover.
+ [ ] Implement simple dynamic rule, and document how to further scale it.
+ [ ] Validate correctness and benchmark throughput.
+ [ ] Document comparing sliding window algorithm vs token bucket algorithm.
+ [ ] Document enhanced synchronization mechanism rather than locking.
### Optional
+ [ ] Improve by implementing enhanced synchronization mechanism rather
than locking.
+ [ ] Multi - data centers setup.

### Stacks
1. Java / Spring Boot.
2. Redis.
3. Docker / K8s.
4. JMeter.

