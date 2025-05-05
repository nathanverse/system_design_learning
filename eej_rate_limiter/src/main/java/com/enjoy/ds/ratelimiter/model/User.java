package com.enjoy.ds.ratelimiter.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private UUID id;
  private String username;
  private String password; // Store hashed passwords!
  private String role; // e.g., "ROLE_USER", "ROLE_ADMIN"

    public User(UUID uuid, String admin, String s, String admin1) {
    }

    public static User copy(User original) {
    return new User(original.id, original.username, original.password, original.role);
  }
}
