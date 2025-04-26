# Rate limiter
## 1. Overview

This project aims to create a simple distributed rate limiter using Java Spring Boot and Redis. 

The implementation of this project will be based heavily on Reactive Streaming, which allows us to build an asynchronous app, significantly
improving the throughput of the rate limiter. For those still don't know such concept, I recommend to learn it first before delving into code.

However, you can still read the following documentation which will provide you with a general idea of how the rate limiter is implemented.

## 2. Implementation

### 2.1. Checking rate limiter condition.
To make it simple, the rate limiter of this project will be based on API per user, more specifically the number of posts user can upload
per second in a social media app.

Thinking of the problem, we must have a way to check the condition before allowing the request to proceed. Such cutting-edge problem is
well addressed by Aspect Oriented Programming (AOP) concept in Java. AOP allows you to do side effects on Java classes, like before, after, or
around a method call. Leveraging it, we can make our rate limiter validation process as a side effect before allowing it to access our
API controller.

You can find the declaration of an aspect responsible for checking condition in `RateLimiterAspect`.
### 2.2. Rate limiter logic
The algorithm we use in this project for the rate limiter is **Sliding window algorithm**. Although we only use one algorithm, the component is designed
for readers to implement different one as desired by extending the class `UserBasedRateLimiter`. This interface rate limits on api name and user id.
The sliding window algorithm is implemented in `SlidingWindowUserBasedRateLimiter`

The rate limiter has two versions, one using built-in memory, and one using Redis as distributed memory. We will examine the built-in one first before discover
distributed approach in 2.2.

#### Core logic with `ConcurrentHashmap`
Recalling sliding window algorithm, it requires us to store the set of request timestamps that a user send per API. As each request comes, we
count the number of request in the time window until the current request timestamp. If the count is greater than the limit, we block the request from
proceeding, otherwise we add this request timestamp to the current set and allow the request to pass.

`ConcurrentHashMap` is a best choice to address this problem not only for the logic but for the concurrency requirement, where we need to ensure that
if two requests come at the same time at the time the set has `limit-1` items, only one request is allowed to pass. Following is an illustration of
how each entry in the map would be:

```
 {user_1, post_api} -> { 1, 2, 3 }
 {user_2, payment_api} -> { 1, 2 }
```

In addition, this algorithm can lead to memory bloat if the system has many APIS, serving for a huge number of user. Therefore, mindful implementations need to 
remove all request timestamps that are not in the latest window and put time-to-live which is equal to the window duration to each entry in the hashmap.

You can find the code logic in `InMemorySlidingWindowRateLimiterStorage`.

#### Simple dynamic loading.
Give that each API needs a different limit requirements, the rate limiter needs a way to identify the limit of each API.

To implement a dynamic rate limiter in a Spring Boot application with API-specific limits:
1. Store API rate limits (e.g., in a database or Redis).
2. Create a service to retrieve limits based on API identifiers.
3. Use a custom Java annotation on API methods, including the API identifier.
4. Employ Spring AOP to intercept annotated methods, fetch the corresponding limit via the service, and apply rate limiting before method execution.

Following is the summarization of the process.

![AOP_rate_limiter](/eej_rate_limiter/static/AOP_rate_limiter.jpg)

You can look through these components to understand how dynamic loading works 
`PostController`, `RateLimit`, `RateLimiterRuleService`, `SlidingWindowUserBasedRateLimiter`.

In real life, to ensure low latency requirement for the rate limiter, you may need to load rules from Redis and use a bunch of workers
to update it periodically.

### 2.3. Distributed rate limiters.
For a distributed rate limiter scaled across multiple instances, shared state is required. Redis is used for this shared memory.

While standard Redis transactions use optimistic locking, potentially allowing race conditions (see [Redis transaction](https://redis.io/docs/latest/develop/interact/transactions/#optimistic-locking-using-check-and-set))
, Redis Lua scripting offers atomic execution. This prevents race conditions by ensuring all operations 
within the script complete without interruption. Although Lua scripts block other commands during execution, 
their speed typically makes this negligible in practice.

Therefore, our distributed rate limiter leverages Redis Lua scripts to atomically manage state. 
Operations on the rate limiter's entry are wrapped in a script sent to Redis for execution. 
Refer to `DistributedSlidingWindowRateLimiterStorage` for the implementation details.

## 3. Deploy
### Testing
For testing, simply roll up a `Redis` container on port `6379` and use your Gradle debugger to run embedded Spring
app.

You need to login with the username `admin`, password `admin` on `http://localhost:8080/api/auth/login` before accessing
to post api `http://localhost:8080/api/posts/1`. Current rate limit for creating post is 2 posts per second.
### Minikube playground
The project also provides helm artifacts to deploy the app on Minikube K8s cluster. You can take a look and use it
to deploy on your real k8s cluster.

Before of all, ensuring you have installed `Docker`, `Helm`, and `Minikube`. Then, following below commands.

```bash
# 1. Init minikube
$ minikube start

# 2. Push the app image
$ ./build_and_push.sh [your_docker_repo]:[your_docker_tag]

# 3. Fix ./k8s/values.yml to reference to your image.repository and image.tag

# 4. Install helm
$ helm install rate-limiter ./helm -f k8s/values.yml -n rate-limiter

# 5. Channel minikube service NodePort to local port
$ kubectl port-forward deployment/rate-limiter 8080:8080 -n rate-limiter
```

You can now curl to port 8080 to test the rate limiter.