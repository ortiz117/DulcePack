package com.dulcepack.backend.identity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PasswordResetNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetNotificationListener.class);

    private final PasswordResetNotifier notifier;

    public PasswordResetNotificationListener(PasswordResetNotifier notifier) {
        this.notifier = notifier;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        try {
            notifier.send(event.email(), event.rawToken());
        } catch (RuntimeException exception) {
            log.error("Password reset notification delivery failed for {}", event.email(), exception);
        }
    }
}
