package com.dulcepack.backend.identity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpPasswordResetNotifier implements PasswordResetNotifier {
    private static final Logger log = LoggerFactory.getLogger(NoOpPasswordResetNotifier.class);

    @Override
    public void send(String email, String rawToken) {
        log.warn("Password reset email delivery is disabled for {}", email);
    }
}
