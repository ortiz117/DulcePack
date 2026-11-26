package com.dulcepack.backend.catalog.api;

import java.math.BigDecimal;
import java.util.List;

public record CatalogProductResponse(
        String id,
        String name,
        String shortName,
        String description,
        BigDecimal price,
        String currency,
        String category,
        String kind,
        String flavor,
        String imageUrl,
        int available,
        String stockStatus,
        List<String> tags) {
}
