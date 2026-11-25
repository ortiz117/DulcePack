package com.dulcepack.backend.cart;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cart")
public record CartProperties(BigDecimal shippingFee, String defaultCoupon) {
    public BigDecimal shippingFee() {
        return shippingFee == null ? new BigDecimal("79.00") : shippingFee;
    }

    public String defaultCoupon() {
        return defaultCoupon == null || defaultCoupon.isBlank() ? "DULCE50" : defaultCoupon;
    }
}
