package com.dulcepack.backend.cart.api;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        int itemCount,
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal discount,
        BigDecimal total,
        String couponCode) {
}
