package com.dulcepack.backend.identity.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dulcepack.backend.identity.domain.LoginAttemptEntity;
import com.dulcepack.backend.identity.repository.LoginAttemptRepository;
import com.dulcepack.backend.security.SecurityProperties;
import com.dulcepack.backend.security.TokenService;
import com.dulcepack.backend.shared.error.RateLimitException;

@Service
public class LoginAttemptService {
    private final LoginAttemptRepository attempts;
    private final SecurityProperties properties;
    private final TokenService tokens;
    private final Clock clock;

    public LoginAttemptService(LoginAttemptRepository attempts, SecurityProperties properties,
            TokenService tokens, Clock clock) {
        this.attempts = attempts;
        this.properties = properties;
        this.tokens = tokens;
        this.clock = clock;
    }

    public String identifier(String normalizedEmail, String ipAddress) {
        return tokens.hash(normalizedEmail);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void assertAllowed(String identifier) {
        Instant now = clock.instant();
        attempts.findForUpdate(identifier).ifPresent(attempt -> {
            if (attempt.getBlockedUntil() != null && attempt.getBlockedUntil().isAfter(now)) {
                throw new RateLimitException("LOGIN_RATE_LIMITED", "Too many login attempts; try again later");
            }
            if (attempt.getWindowStartedAt().plus(Duration.ofMinutes(properties.loginWindowMinutes())).isBefore(now)) {
                attempts.delete(attempt);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String identifier) {
        Instant now = clock.instant();
        LoginAttemptEntity attempt = attempts.findForUpdate(identifier)
                .orElseGet(() -> LoginAttemptEntity.firstFailure(identifier, now));
        attempt.recordFailure(now,
                now.minus(Duration.ofMinutes(properties.loginWindowMinutes())),
                properties.maxLoginFailures(),
                now.plus(Duration.ofMinutes(properties.loginBlockMinutes())));
        attempts.save(attempt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String identifier) {
        attempts.deleteById(identifier);
    }
}
