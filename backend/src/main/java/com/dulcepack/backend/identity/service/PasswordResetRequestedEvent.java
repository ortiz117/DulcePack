package com.dulcepack.backend.identity.service;

public record PasswordResetRequestedEvent(String email, String rawToken) {
}
