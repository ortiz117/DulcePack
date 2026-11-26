package com.dulcepack.backend.identity.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap-admin")
public record AdminBootstrapProperties(String email, String password, String displayName) {
}
