package com.enjoy.ds.ratelimiter;

import com.enjoy.ds.ratelimiter.model.User;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final User adminUser;
  private final User user;

  @Autowired
  public UserService() {
    PasswordEncoder encoder = new BCryptPasswordEncoder();
    this.adminUser =
        new User(
            UUID.randomUUID(),
            "admin",
            "$2a$10$IJghHStG8tMvZnOvdblT7.hFnrnb98LEuNhCok0SpgnQ.A/PHD0eq",
            "ADMIN");

    this.user =
        new User(
            UUID.randomUUID(),
            "user",
            "$2a$10$IJghHStG8tMvZnOvdblT7.hFnrnb98LEuNhCok0SpgnQ.A/PHD0eq",
            "USER");
  }

  public Mono<User> findByUserName(String username) {
    if ("admin".equals(username)) {
      return Mono.just(User.copy(adminUser));
    } else if ("user".equals(username)) {
      return Mono.just(User.copy(user));
    }

    return Mono.empty();
  }
}
