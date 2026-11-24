package com.dulcepack.backend.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dulcepack.backend.identity.domain.UserEntity;
import com.dulcepack.backend.identity.domain.UserSessionEntity;
import com.dulcepack.backend.identity.domain.UserStatus;
import com.dulcepack.backend.identity.repository.UserSessionRepository;
import com.dulcepack.backend.shared.error.UnauthorizedException;

@Service
public class SessionService {
    private final UserSessionRepository sessions;
    private final TokenService tokens;
    private final SecurityProperties properties;
    private final Clock clock;

    public SessionService(UserSessionRepository sessions, TokenService tokens,
            SecurityProperties properties, Clock clock) {
        this.sessions = sessions;
        this.tokens = tokens;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public SessionTokens create(UserEntity user, String ipAddress, String userAgent) {
        Instant now = clock.instant();
        SessionTokens generated = generate(now);
        sessions.save(UserSessionEntity.create(user, tokens.hash(generated.accessToken()),
                tokens.hash(generated.refreshToken()), generated.accessExpiresAt(),
                generated.refreshExpiresAt(), now, truncate(ipAddress, 64), truncate(userAgent, 500)));
        return generated;
    }

    @Transactional
    public Optional<CurrentUser> authenticate(String rawAccessToken) {
        if (rawAccessToken == null || rawAccessToken.isBlank()) {
            return Optional.empty();
        }
        Instant now = clock.instant();
        return sessions.findByAccessTokenHashAndRevokedAtIsNull(tokens.hash(rawAccessToken))
                .filter(session -> session.getAccessExpiresAt().isAfter(now))
                .filter(session -> session.getUser().getStatus() == UserStatus.ACTIVE)
                .map(session -> {
                    session.touch(now);
                    UserEntity user = session.getUser();
                    return new CurrentUser(user.getId(), user.getEmail(), user.getRole());
                });
    }

    @Transactional
    public SessionTokens rotate(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Refresh token is invalid");
        }
        Instant now = clock.instant();
        UserSessionEntity session = sessions
                .findByRefreshTokenHashAndRevokedAtIsNull(tokens.hash(rawRefreshToken))
                .filter(value -> value.getRefreshExpiresAt().isAfter(now))
                .filter(value -> value.getUser().getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new UnauthorizedException("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));
        SessionTokens generated = generate(now);
        session.rotate(tokens.hash(generated.accessToken()), tokens.hash(generated.refreshToken()),
                generated.accessExpiresAt(), generated.refreshExpiresAt(), now);
        return generated;
    }

    @Transactional
    public void revoke(String rawAccessToken, String rawRefreshToken) {
        Instant now = clock.instant();
        Optional<UserSessionEntity> session = Optional.empty();
        if (rawAccessToken != null && !rawAccessToken.isBlank()) {
            session = sessions.findByAccessTokenHashAndRevokedAtIsNull(tokens.hash(rawAccessToken));
        }
        if (session.isEmpty() && rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            session = sessions.findByRefreshTokenHashAndRevokedAtIsNull(tokens.hash(rawRefreshToken));
        }
        session.ifPresent(value -> value.revoke(now));
    }

    @Transactional
    public void revokeAll(UserEntity user) {
        sessions.revokeAllByUserId(user.getId(), clock.instant());
    }

    private SessionTokens generate(Instant now) {
        return new SessionTokens(tokens.generate(), tokens.generate(),
                now.plus(Duration.ofMinutes(properties.accessTokenMinutes())),
                now.plus(Duration.ofDays(properties.refreshTokenDays())));
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
