package com.dulcepack.backend.cart.api;

import java.math.BigDecimal;

public record CartItemResponse(
        String productSlug,
        String name,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal,
        int available) {
}
