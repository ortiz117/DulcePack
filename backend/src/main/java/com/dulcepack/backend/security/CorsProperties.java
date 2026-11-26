package com.dulcepack.backend.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
    public List<String> allowedOrigins() {
        return allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
