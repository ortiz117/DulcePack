package com.dulcepack.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        boolean secureCookies,
        long accessTokenMinutes,
        long refreshTokenDays,
        long passwordResetMinutes,
        int maxLoginFailures,
        long loginWindowMinutes,
        long loginBlockMinutes) {
}
