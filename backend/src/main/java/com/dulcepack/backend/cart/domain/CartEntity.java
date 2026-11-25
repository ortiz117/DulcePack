package com.dulcepack.backend.cart.domain;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "carts")
public class CartEntity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "coupon_code", length = 40)
    private String couponCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItemEntity> items = new ArrayList<>();

    protected CartEntity() {
    }

    public CartEntity(UUID id, UUID userId, String tokenHash, String couponCode, Instant now, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.couponCode = couponCode;
        this.createdAt = now;
        this.updatedAt = now;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }

    public String getTokenHash() { return tokenHash; }
    public String getCouponCode() { return couponCode; }
    public List<CartItemEntity> getItems() { return items; }

    public Instant getExpiresAt() { return expiresAt; }

    public void applyCoupon(String code, Instant now) {
        this.couponCode = code;
        this.updatedAt = now;
    }

    public void clearCoupon(Instant now) {
        this.couponCode = null;
        this.updatedAt = now;
    }

    public void touch(Instant now) {
        this.updatedAt = now;
        this.expiresAt = now.plus(Duration.ofDays(7));
    }
}
