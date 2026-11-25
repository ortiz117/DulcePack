package com.dulcepack.backend.security;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class SessionCookieService {
    public static final String ACCESS_COOKIE = "DP_SESSION";
    public static final String REFRESH_COOKIE = "DP_REFRESH";

    private final SecurityProperties properties;

    public SessionCookieService(SecurityProperties properties) {
        this.properties = properties;
    }

    public void write(HttpServletResponse response, SessionTokens tokens) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(ACCESS_COOKIE, tokens.accessToken(), "/",
                Duration.ofMinutes(properties.accessTokenMinutes())).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(REFRESH_COOKIE, tokens.refreshToken(), "/api/auth",
                Duration.ofDays(properties.refreshTokenDays())).toString());
    }

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(ACCESS_COOKIE, "", "/", Duration.ZERO).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(REFRESH_COOKIE, "", "/api/auth", Duration.ZERO).toString());
    }

    private ResponseCookie cookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.secureCookies())
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}
