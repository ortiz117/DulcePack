package com.dulcepack.backend.identity.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_sessions")
public class UserSessionEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "access_token_hash", nullable = false, unique = true, length = 64)
    private String accessTokenHash;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 64)
    private String refreshTokenHash;

    @Column(name = "access_expires_at", nullable = false)
    private Instant accessExpiresAt;

    @Column(name = "refresh_expires_at", nullable = false)
    private Instant refreshExpiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_used_at", nullable = false)
    private Instant lastUsedAt;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    protected UserSessionEntity() {
    }

    public static UserSessionEntity create(UserEntity user, String accessHash, String refreshHash,
            Instant accessExpiresAt, Instant refreshExpiresAt, Instant now, String ipAddress, String userAgent) {
        UserSessionEntity session = new UserSessionEntity();
        session.id = UUID.randomUUID();
        session.user = user;
        session.accessTokenHash = accessHash;
        session.refreshTokenHash = refreshHash;
        session.accessExpiresAt = accessExpiresAt;
        session.refreshExpiresAt = refreshExpiresAt;
        session.createdAt = now;
        session.lastUsedAt = now;
        session.ipAddress = ipAddress;
        session.userAgent = userAgent;
        return session;
    }

    public UUID getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getAccessTokenHash() { return accessTokenHash; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public Instant getAccessExpiresAt() { return accessExpiresAt; }
    public Instant getRefreshExpiresAt() { return refreshExpiresAt; }
    public Instant getRevokedAt() { return revokedAt; }

    public void touch(Instant now) { this.lastUsedAt = now; }

    public void rotate(String accessHash, String refreshHash, Instant accessExpiresAt,
            Instant refreshExpiresAt, Instant now) {
        this.accessTokenHash = accessHash;
        this.refreshTokenHash = refreshHash;
        this.accessExpiresAt = accessExpiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
        this.lastUsedAt = now;
    }

    public void revoke(Instant now) {
        if (this.revokedAt == null) {
            this.revokedAt = now;
        }
    }
}
