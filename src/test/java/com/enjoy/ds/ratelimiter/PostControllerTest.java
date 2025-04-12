package com.enjoy.ds.ratelimiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.enjoy.ds.ratelimiter.cache.RedisService;
import com.enjoy.ds.ratelimiter.core.model.APIRule;
import com.enjoy.ds.ratelimiter.core.model.RateLimiterRuleService;
import com.enjoy.ds.ratelimiter.model.Post;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootTest
@AutoConfigureWebTestClient
public class PostControllerTest extends AbstractControllerTest {
  private PostController postController;

  private WebTestClient webTestClient;

  @Autowired private RedisService redisService;

  @BeforeEach
  void setUp(ApplicationContext context) {
    webTestClient = WebTestClient.bindToApplicationContext(context).build();
    redisService.flushAll();
    Mockito.reset(rateLimiterRuleService);
  }

  private WebTestClient.RequestHeadersSpec<?> getPostReqSpec(String jwt, Integer index) {
    return webTestClient
        .get()
        .uri(String.format("/api/posts/%d", index))
        .header("Authorization", "Bearer " + jwt);
  }

  @MockitoBean private RateLimiterRuleService rateLimiterRuleService;

  @Test
  void testRateLimiter_51ConcurrentRequests() {
    String jwt = jwt();

    when(rateLimiterRuleService.getRule("post_a_post"))
        .thenReturn(Mono.just(new APIRule("post_a_post", 50, 2000)));

    List<Mono<Boolean>> requests =
        IntStream.range(0, 52)
            .mapToObj(
                i ->
                    getPostReqSpec(jwt, i)
                        .exchange()
                        .returnResult(Post.class)
                        .getResponseBody()
                        .map(post -> true)
                        .onErrorReturn(false)
                        .next())
            .toList();

    AtomicInteger successReq = new AtomicInteger(0);
    AtomicInteger failReq = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(52);

    Flux.merge(requests)
        .subscribe(
            result -> {
              if (result) successReq.incrementAndGet();
              else failReq.incrementAndGet();
              latch.countDown();
            });

    try {
      boolean allDone = latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      assertThat(false).isTrue();
    }

    assertThat(successReq.get()).isEqualTo(50);
    assertThat(failReq.get()).isEqualTo(2);
  }

  @Test
  void testRateLimiter_50RequestsAtStart1RequestAtTheRear() {
    String jwt = jwt();

    when(rateLimiterRuleService.getRule("post_a_post"))
        .thenReturn(Mono.just(new APIRule("post_a_post", 50, 2000)));

    Scheduler elasticScheduler = Schedulers.boundedElastic();

    List<Mono<Boolean>> requests =
        IntStream.range(0, 52)
            .mapToObj(
                i -> {
                  Mono<Boolean> temp =
                      Mono.defer(
                              () ->
                                  getPostReqSpec(jwt, i)
                                      .exchange()
                                      .returnResult(Post.class)
                                      .getResponseBody()
                                      .map(post -> true)
                                      .onErrorReturn(false)
                                      .next())
                          .subscribeOn(elasticScheduler);

                  if (i >= 50) {
                    return temp.delaySubscription(
                        Duration.of(1900, ChronoUnit.MILLIS), elasticScheduler);
                  }
                  return temp;
                })
            .toList();

    AtomicInteger successReq = new AtomicInteger(0);
    AtomicInteger failReq = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(52);

    Flux.merge(requests)
        .subscribe(
            result -> {
              if (result) successReq.incrementAndGet();
              else failReq.incrementAndGet();
              latch.countDown();
            });

    try {
      boolean allDone = latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      assertThat(false).isTrue();
    }

    assertThat(successReq.get()).isEqualTo(50);
    assertThat(failReq.get()).isEqualTo(2);
  }

  @Test
  void testRateLimiter__25reqsAt1_9s__26reqsAt2_1s() {
    String jwt = jwt();

    when(rateLimiterRuleService.getRule("post_a_post"))
        .thenReturn(Mono.just(new APIRule("post_a_post", 50, 2000)));

    Scheduler elasticScheduler = Schedulers.boundedElastic();

    List<Mono<Boolean>> requests =
        IntStream.range(0, 51)
            .mapToObj(
                i -> {
                  Mono<Boolean> temp =
                      Mono.defer(
                              () ->
                                  getPostReqSpec(jwt, i)
                                      .exchange()
                                      .returnResult(Post.class)
                                      .getResponseBody()
                                      .map(post -> true)
                                      .onErrorReturn(false)
                                      .next())
                          .subscribeOn(elasticScheduler);

                  if (i <= 24) {
                    return temp.delaySubscription(
                        Duration.of(1900, ChronoUnit.MILLIS), elasticScheduler);
                  } else {
                    return temp.delaySubscription(
                        Duration.of(2100, ChronoUnit.MILLIS), elasticScheduler);
                  }
                })
            .toList();

    AtomicInteger successReq = new AtomicInteger(0);
    AtomicInteger failReq = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(51);

    Flux.merge(requests)
        .subscribe(
            result -> {
              if (result) successReq.incrementAndGet();
              else failReq.incrementAndGet();
              latch.countDown();
            });

    try {
      boolean allDone = latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      assertThat(false).isTrue();
    }

    assertThat(successReq.get()).isEqualTo(50);
    assertThat(failReq.get()).isEqualTo(1);
  }

  @Test
  void testRateLimiter__52reqEvenlySpaced() {
    String jwt = jwt();

    when(rateLimiterRuleService.getRule("post_a_post"))
        .thenReturn(Mono.just(new APIRule("post_a_post", 50, 2000)));

    Scheduler elasticScheduler = Schedulers.boundedElastic();

    List<Mono<Boolean>> requests =
        IntStream.range(0, 52)
            .mapToObj(
                i -> {
                  Mono<Boolean> temp =
                      Mono.defer(
                              () ->
                                  getPostReqSpec(jwt, i)
                                      .exchange()
                                      .returnResult(Post.class)
                                      .getResponseBody()
                                      .map(post -> true)
                                      .onErrorReturn(false)
                                      .next())
                          .subscribeOn(elasticScheduler);

                  long delayMilli = i * (2L * 1000 / 50);
                  if (i >= 50) {
                    delayMilli = delayMilli + 100;
                  }
                  return temp.delaySubscription(
                      Duration.of(delayMilli, ChronoUnit.MILLIS), elasticScheduler);
                })
            .toList();

    AtomicInteger successReq = new AtomicInteger(0);
    AtomicInteger failReq = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(52);

    Flux.merge(requests)
        .subscribe(
            result -> {
              if (result) successReq.incrementAndGet();
              else failReq.incrementAndGet();
              latch.countDown();
            });

    try {
      boolean allDone = latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      assertThat(false).isTrue();
    }

    assertThat(successReq.get()).isEqualTo(52);
    assertThat(failReq.get()).isEqualTo(0);
  }
}
