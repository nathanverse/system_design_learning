package com.enjoy.ds.ratelimiter.auth;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-in-ms}")
    private Long jwtExpirationInMs;

    private SecretKey key;
    private JwtParser parser;

    private SecretKey key() {
        if (key == null) {
            key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }

        return key;
    }

    private JwtParser parser() {
        if (parser == null) {
            parser = Jwts.parser().verifyWith(key()).build();
        }

        return parser;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder().subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key(), Jwts.SIG.HS512)
                .compact();
    }

    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                parser().parseSignedClaims(token);
                return true;
            } catch (Exception ex) {
                return false;
            }
        });
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        Object rolesObject = null;
        try {
            rolesObject = parser().parseSignedClaims(token).getPayload().get("roles", List.class);

            if(rolesObject == null){
                return Collections.emptyList();
            }

            return ((List<?>) rolesObject)
                    .stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

        } catch (ClassCastException e) {
            System.err.println("Roles claim is not a List<String>");
            return null; // Or throw an exception
        }
    }

    public Mono<String> getUsernameFromTokenReactive(String token) {
        return Mono.fromCallable(
                () -> parser().parseSignedClaims(token).getPayload().getSubject()
        );
    }

    public String getUsernameFromToken(String token) {
        return parser().parseSignedClaims(token).getPayload().getSubject();
    }
}
