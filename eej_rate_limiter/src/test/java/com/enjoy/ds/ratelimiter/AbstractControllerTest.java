package com.enjoy.ds.ratelimiter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AbstractControllerTest {
  @Value("${app.jwt.secret}")
  private String jwtSecret;

  private SecretKey key;

  private SecretKey key() {
    if (key == null) {
      key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    return key;
  }

  protected String jwt() {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + TimeUnit.DAYS.toMillis(1));
    List<String> stringList = new ArrayList<>();
    stringList.add("ROLE_ADMIN");

    return Jwts.builder()
        .subject(UUID.randomUUID().toString())
        .claim("roles", stringList)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(key(), Jwts.SIG.HS512)
        .compact();
  }
}
