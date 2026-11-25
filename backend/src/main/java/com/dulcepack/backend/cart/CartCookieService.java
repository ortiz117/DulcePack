package com.dulcepack.backend.cart;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.dulcepack.backend.security.SecurityProperties;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class CartCookieService {
    public static final String CART_COOKIE = "DP_CART";

    private final SecurityProperties properties;

    public CartCookieService(SecurityProperties properties) {
        this.properties = properties;
    }

    public void write(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(token, Duration.ofDays(30)).toString());
    }


    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(CART_COOKIE, value)
                .httpOnly(true)
                .secure(properties.secureCookies())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
