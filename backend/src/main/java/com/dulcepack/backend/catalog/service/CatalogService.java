package com.dulcepack.backend.catalog.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dulcepack.backend.catalog.api.CatalogProductResponse;
import com.dulcepack.backend.catalog.domain.ProductEntity;
import com.dulcepack.backend.catalog.domain.ProductFlavor;
import com.dulcepack.backend.catalog.repository.ProductRepository;
import com.dulcepack.backend.shared.error.ApiException;

@Service
public class CatalogService {
    private final ProductRepository products;

    public CatalogService(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<CatalogProductResponse> search(String query, String category, String flavor,
            BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0
                || minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICE_RANGE", "Price range is invalid");
        }
        ProductFlavor parsedFlavor = parseFlavor(flavor);
        return products.search(normalizeNullable(query), normalizeNullable(category), parsedFlavor, minPrice, maxPrice)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CatalogProductResponse detail(String slug) {
        return products.findBySlug(normalizeRequired(slug))
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product was not found"));
    }

    private CatalogProductResponse toResponse(ProductEntity product) {
        var inventory = product.getInventory();
        String category = product.getCategory().getSlug();
        String flavor = product.getFlavor() == null
                ? null
                : product.getFlavor().name().toLowerCase(Locale.ROOT);
        String imageUrl = product.getImage() == null
                ? null
                : "/api/catalog/products/" + product.getSlug() + "/image";
        return new CatalogProductResponse(product.getSlug(), product.getName(), product.getShortName(),
                product.getDescription(), product.getPrice(), product.getCurrency(), category, category, flavor,
                imageUrl, inventory.getAvailable(), inventory.getStatus().name(), parseTags(product.getTags()));
    }

    private ProductFlavor parseFlavor(String flavor) {
        String normalized = normalizeNullable(flavor);
        if (normalized == null) {
            return null;
        }
        try {
            return ProductFlavor.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FLAVOR", "Flavor is invalid");
        }
    }

    private List<String> parseTags(String value) {
        if (!StringUtils.hasText(value) || "[]".equals(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    public static String normalizeSearchText(String value) {
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? normalizeSearchText(value) : null;
    }

    private String normalizeRequired(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? "" : normalized;
    }
}
