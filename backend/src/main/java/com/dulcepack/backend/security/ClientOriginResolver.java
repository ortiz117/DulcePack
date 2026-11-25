package com.dulcepack.backend.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientOriginResolver {
    private final SecurityProperties properties;

    public ClientOriginResolver(SecurityProperties properties) {
        this.properties = properties;
    }

    public String resolve(HttpServletRequest request) {
        if (properties.trustForwardedFor()) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String candidate = forwarded.split(",", 2)[0].strip();
                if (candidate.length() <= 64 && candidate.matches("[0-9A-Fa-f:.]+")) {
                    return candidate;
                }
            }
        }
        return request.getRemoteAddr();
    }
}
