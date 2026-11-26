package com.dulcepack.backend.identity.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() {
    }

    public static UserEntity client(String email, String displayName, String passwordHash, Instant now) {
        return create(email, displayName, passwordHash, UserRole.CLIENT, now);
    }

    public static UserEntity administrator(String email, String displayName, String passwordHash, Instant now) {
        return create(email, displayName, passwordHash, UserRole.ADMIN, now);
    }

    private static UserEntity create(String email, String displayName, String passwordHash, UserRole role, Instant now) {
        UserEntity user = new UserEntity();
        user.id = UUID.randomUUID();
        user.email = email;
        user.displayName = displayName;
        user.passwordHash = passwordHash;
        user.role = role;
        user.status = UserStatus.ACTIVE;
        user.passwordChangedAt = now;
        user.createdAt = now;
        user.updatedAt = now;
        return user;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public Instant getPasswordChangedAt() { return passwordChangedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void changePassword(String newPasswordHash, Instant now) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = now;
        this.updatedAt = now;
    }

    public void changeStatus(UserStatus newStatus, Instant now) {
        this.status = newStatus;
        this.updatedAt = now;
    }
}
