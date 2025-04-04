package com.enjoy.ds.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;


@Data
public class User {
    private UUID id;
    private String username;
    private String password; // Store hashed passwords!
    private String role; // e.g., "ROLE_USER", "ROLE_ADMIN"
}