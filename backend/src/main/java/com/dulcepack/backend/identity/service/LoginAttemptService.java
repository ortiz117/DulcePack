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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void assertLoginOriginAllowed(String origin) {
        if (isBlocked(key("login-origin", origin), clock.instant(), properties.loginWindowMinutes())) {
            throw new RateLimitException("LOGIN_RATE_LIMITED", "Too many login attempts; try again later");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recordInvalidLogin(String normalizedEmail, String origin) {
        Instant now = clock.instant();
        String accountKey = key("login-account", normalizedEmail);
        String originKey = key("login-origin", origin);
        if (isBlocked(accountKey, now, properties.loginWindowMinutes())
                || isBlocked(originKey, now, properties.loginWindowMinutes())) {
            return true;
        }
        record(accountKey, now, properties.maxLoginFailures(),
                properties.loginWindowMinutes(), properties.loginBlockMinutes());
        record(originKey, now, properties.maxOriginLoginFailures(),
                properties.loginWindowMinutes(), properties.loginBlockMinutes());
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccessfulLogin(String normalizedEmail) {
        attempts.deleteById(key("login-account", normalizedEmail));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void acquireRegistrationPermit(String origin) {
        acquirePermit(key("register-origin", origin), properties.maxRegistrationRequests(),
                properties.registrationWindowMinutes(), properties.registrationBlockMinutes(),
                "REGISTRATION_RATE_LIMITED", "Too many registration attempts; try again later");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void acquirePasswordResetPermit(String normalizedEmail, String origin) {
        Instant now = clock.instant();
        String accountKey = key("password-reset-account", normalizedEmail);
        String originKey = key("password-reset-origin", origin);
        if (isBlocked(accountKey, now, properties.passwordResetWindowMinutes())
                || isBlocked(originKey, now, properties.passwordResetWindowMinutes())) {
            throw new RateLimitException("PASSWORD_RESET_RATE_LIMITED",
                    "Too many password reset requests; try again later");
        }
        record(accountKey, now, properties.maxPasswordResetRequests(),
                properties.passwordResetWindowMinutes(), properties.passwordResetBlockMinutes());
        record(originKey, now, properties.maxOriginPasswordResetRequests(),
                properties.passwordResetWindowMinutes(), properties.passwordResetBlockMinutes());
    }

    private void acquirePermit(String identifier, int maximum, long windowMinutes, long blockMinutes,
            String code, String message) {
        Instant now = clock.instant();
        if (isBlocked(identifier, now, windowMinutes)) {
            throw new RateLimitException(code, message);
        }
        record(identifier, now, maximum, windowMinutes, blockMinutes);
    }

    private boolean isBlocked(String identifier, Instant now, long windowMinutes) {
        return attempts.findForUpdate(identifier)
                .map(attempt -> {
                    if (attempt.getBlockedUntil() != null && attempt.getBlockedUntil().isAfter(now)) {
                        return true;
                    }
                    if (attempt.getWindowStartedAt().plus(Duration.ofMinutes(windowMinutes)).isBefore(now)) {
                        attempts.delete(attempt);
                    }
                    return false;
                })
                .orElse(false);
    }

    private void record(String identifier, Instant now, int maximum, long windowMinutes, long blockMinutes) {
        LoginAttemptEntity attempt = attempts.findForUpdate(identifier)
                .orElseGet(() -> LoginAttemptEntity.firstFailure(identifier, now));
        attempt.recordFailure(now, now.minus(Duration.ofMinutes(windowMinutes)), maximum,
                now.plus(Duration.ofMinutes(blockMinutes)));
        attempts.save(attempt);
    }

    private String key(String namespace, String value) {
        return tokens.hash(namespace + ":" + (value == null || value.isBlank() ? "unknown" : value));
    }
}
