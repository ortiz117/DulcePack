package com.dulcepack.backend.catalog.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dulcepack.backend.catalog.api.CatalogPagedResponse;
import com.dulcepack.backend.catalog.api.CatalogProductResponse;
import com.dulcepack.backend.catalog.api.CategoryResponse;
import com.dulcepack.backend.catalog.domain.ProductCategoryEntity;
import com.dulcepack.backend.catalog.domain.ProductEntity;
import com.dulcepack.backend.catalog.domain.ProductFlavor;
import com.dulcepack.backend.catalog.repository.ProductCategoryRepository;
import com.dulcepack.backend.catalog.repository.ProductRepository;
import com.dulcepack.backend.shared.error.ApiException;
import jakarta.persistence.criteria.Predicate;

@Service
public class CatalogService {
    public static final int MAX_UNPAGED_RESULTS = 100;

    private final ProductRepository products;
    private final ProductCategoryRepository categories;

    public CatalogService(ProductRepository products, ProductCategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<CatalogProductResponse> search(String query, String category, String flavor,
            BigDecimal minPrice, BigDecimal maxPrice) {
        validateFilters(minPrice, maxPrice);
        ProductFlavor parsedFlavor = parseFlavor(flavor);
        return products.search(escapeLike(normalizeNullable(query)), normalizeNullable(category), parsedFlavor,
                minPrice, maxPrice)
                .stream().limit(MAX_UNPAGED_RESULTS).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CatalogPagedResponse<CatalogProductResponse> searchPage(String query, String category, String flavor,
            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        validateFilters(minPrice, maxPrice);
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Page or size is invalid");
        }
        ProductFlavor parsedFlavor = parseFlavor(flavor);
        String normalizedQuery = escapeLike(normalizeNullable(query));
        String normalizedCategory = normalizeNullable(category);
        Specification<ProductEntity> specification = (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (normalizedQuery != null) {
                predicates.add(builder.like(root.get("searchText"), "%" + normalizedQuery + "%", '\\'));
            }
            if (normalizedCategory != null) {
                predicates.add(builder.equal(root.get("category").get("slug"), normalizedCategory));
            }
            if (parsedFlavor != null) {
                predicates.add(builder.equal(root.get("flavor"), parsedFlavor));
            }
            if (minPrice != null) {
                predicates.add(builder.ge(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(builder.le(root.get("price"), maxPrice));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        Page<CatalogProductResponse> result = products.findAll(specification,
                PageRequest.of(page, size, Sort.by("name")))
                .map(this::toResponse);
        return new CatalogPagedResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() {
        return categories.findAll(Sort.by("name")).stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    private CategoryResponse toCategoryResponse(ProductCategoryEntity category) {
        return new CategoryResponse(category.getSlug(), category.getSlug(), category.getName(), category.getName());
    }

    private void validateFilters(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0
                || minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICE_RANGE", "Price range is invalid");
        }
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

    public static String escapeLike(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? normalizeSearchText(value) : null;
    }

    private String normalizeRequired(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? "" : normalized;
    }
}
