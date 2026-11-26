package com.dulcepack.backend.catalog.api;

import java.util.List;

public record CatalogPagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
