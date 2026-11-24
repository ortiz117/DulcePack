package com.dulcepack.backend.identity.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "login_attempts")
public class LoginAttemptEntity {
    @Id
    @Column(length = 64)
    private String identifier;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "window_started_at", nullable = false)
    private Instant windowStartedAt;

    @Column(name = "blocked_until")
    private Instant blockedUntil;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LoginAttemptEntity() {
    }

    public static LoginAttemptEntity firstFailure(String identifier, Instant now) {
        LoginAttemptEntity attempt = new LoginAttemptEntity();
        attempt.identifier = identifier;
        attempt.failureCount = 0;
        attempt.windowStartedAt = now;
        attempt.updatedAt = now;
        return attempt;
    }

    public int getFailureCount() { return failureCount; }
    public Instant getWindowStartedAt() { return windowStartedAt; }
    public Instant getBlockedUntil() { return blockedUntil; }

    public void recordFailure(Instant now, Instant windowCutoff, int maximumFailures, Instant blockUntil) {
        if (windowStartedAt.isBefore(windowCutoff)) {
            failureCount = 1;
            windowStartedAt = now;
            blockedUntil = null;
        } else {
            failureCount++;
        }
        if (failureCount >= maximumFailures) {
            blockedUntil = blockUntil;
        }
        updatedAt = now;
    }
}
