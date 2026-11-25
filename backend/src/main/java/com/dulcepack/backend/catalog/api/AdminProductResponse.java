package com.dulcepack.backend.catalog.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminProductResponse(
        String id,
        String name,
        String shortName,
        String description,
        BigDecimal price,
        String currency,
        String category,
        List<String> tags,
        String flavor,
        int stock,
        int minimumStock,
        String status,
        ProductImageResponse image,
        Instant updatedAt) {
}
