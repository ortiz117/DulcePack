package com.dulcepack.backend.cart.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "promotions")
public class PromotionEntity {

    @Id
    private String code;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PromotionEntity() {
    }

    public String getCode() { return code; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public boolean isActive() { return active; }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
