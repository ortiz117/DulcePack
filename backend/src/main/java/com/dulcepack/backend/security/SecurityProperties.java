package com.dulcepack.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        boolean secureCookies,
        long accessTokenMinutes,
        long refreshTokenDays,
        long passwordResetMinutes,
        int maxLoginFailures,
        int maxOriginLoginFailures,
        long loginWindowMinutes,
        long loginBlockMinutes,
        int maxRegistrationRequests,
        long registrationWindowMinutes,
        long registrationBlockMinutes,
        int maxPasswordResetRequests,
        int maxOriginPasswordResetRequests,
        long passwordResetWindowMinutes,
        long passwordResetBlockMinutes,
        int maxCartRequests,
        long cartWindowMinutes,
        long cartBlockMinutes,
        boolean trustForwardedFor) {
}
