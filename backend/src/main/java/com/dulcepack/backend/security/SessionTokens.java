package com.dulcepack.backend.security;

import java.time.Instant;

public record SessionTokens(
        String accessToken,
        String refreshToken,
        Instant accessExpiresAt,
        Instant refreshExpiresAt) {
}
