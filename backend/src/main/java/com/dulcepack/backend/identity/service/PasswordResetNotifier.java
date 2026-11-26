package com.dulcepack.backend.identity.service;

public interface PasswordResetNotifier {
    void send(String email, String rawToken);
}
