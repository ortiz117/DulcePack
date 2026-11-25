package com.dulcepack.backend.catalog.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dulcepack.backend.catalog.api.AdminProductResponse;
import com.dulcepack.backend.catalog.api.ProductImageResponse;
import com.dulcepack.backend.catalog.api.UpsertProductRequest;
import com.dulcepack.backend.catalog.domain.ProductEntity;
import com.dulcepack.backend.catalog.domain.ProductFlavor;
import com.dulcepack.backend.catalog.domain.ProductImageEntity;
import com.dulcepack.backend.catalog.domain.ProductInventoryEntity;
import com.dulcepack.backend.catalog.repository.ProductCategoryRepository;
import com.dulcepack.backend.catalog.repository.ProductImageRepository;
import com.dulcepack.backend.catalog.repository.ProductInventoryRepository;
import com.dulcepack.backend.catalog.repository.ProductRepository;
import com.dulcepack.backend.shared.error.ApiException;

@Service
public class AdminCatalogService {
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private final ProductRepository products;
    private final ProductCategoryRepository categories;
    private final ProductInventoryRepository inventory;
    private final ProductImageRepository images;
    private final Clock clock;

    public AdminCatalogService(ProductRepository products, ProductCategoryRepository categories,
            ProductInventoryRepository inventory, ProductImageRepository images, Clock clock) {
        this.products = products;
        this.categories = categories;
        this.inventory = inventory;
        this.images = images;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AdminProductResponse> list(String query) {
        return products.search(normalizeNullable(query), null, null, null, null).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProductResponse detail(String slug) {
        return toResponse(findProduct(slug));
    }

    @Transactional
    public AdminProductResponse create(UpsertProductRequest request, MultipartFile image) {
        String slug = slugify(request.name());
        if (!StringUtils.hasText(slug)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_NAME", "Product name cannot form a valid identifier");
        }
        if (products.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.CONFLICT, "PRODUCT_ALREADY_EXISTS", "A product with this name already exists");
        }
        var category = findCategory(request.category());
        ProductFlavor flavor = parseFlavor(request.flavor());
        Instant now = clock.instant();
        String name = request.name().trim();
        ProductEntity product = products.save(new ProductEntity(UUID.randomUUID(), slug, name, shortName(name),
                request.description().trim(), searchText(name, request.description()), request.price(), category,
                flavor, serializeTags(request.tags()), now));

        ProductInventoryEntity productInventory = new ProductInventoryEntity(product, request.stock(), 0, now);
        inventory.save(productInventory);
        product.attachInventory(productInventory);

        if (image != null && !image.isEmpty()) {
            ProductImageEntity productImage = images.save(createImage(product, image, now));
            product.attachImage(productImage);
        }
        return toResponse(product);
    }

    @Transactional
    public AdminProductResponse update(String slug, UpsertProductRequest request, MultipartFile image) {
        ProductEntity product = products.findBySlugForUpdate(normalizeSlug(slug))
                .orElseThrow(this::productNotFound);
        var category = findCategory(request.category());
        ProductFlavor flavor = parseFlavor(request.flavor());
        Instant now = clock.instant();
        String name = request.name().trim();
        product.update(name, shortName(name), request.description().trim(), searchText(name, request.description()),
                request.price(), category, flavor, serializeTags(request.tags()), now);
        product.getInventory().setAvailable(request.stock(), now);

        if (image != null && !image.isEmpty()) {
            validateImage(image);
            ProductImageEntity current = images.findByProductId(product.getId()).orElse(null);
            if (current == null) {
                current = images.save(createImage(product, image, now));
                product.attachImage(current);
            } else {
                current.replace(safeFileName(image), image.getContentType(), readImage(image), now);
            }
        }
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductImageContent image(String slug) {
        ProductImageEntity image = images.findByProductSlug(normalizeSlug(slug))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_IMAGE_NOT_FOUND", "Product image was not found"));
        return new ProductImageContent(image.getFileName(), image.getContentType(), image.getContent());
    }

    private ProductEntity findProduct(String slug) {
        return products.findBySlug(normalizeSlug(slug)).orElseThrow(this::productNotFound);
    }

    private ApiException productNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product was not found");
    }

    private ProductImageEntity createImage(ProductEntity product, MultipartFile image, Instant now) {
        validateImage(image);
        return new ProductImageEntity(UUID.randomUUID(), product, safeFileName(image), image.getContentType(),
                readImage(image), now);
    }

    private void validateImage(MultipartFile image) {
        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_IMAGE", "Selected file must be an image");
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PRODUCT_IMAGE_TOO_LARGE", "Product image exceeds 5 MB");
        }
    }

    private byte[] readImage(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PRODUCT_IMAGE_UNREADABLE", "Product image could not be read");
        }
    }

    private String safeFileName(MultipartFile image) {
        String original = image.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            return "product-image";
        }
        return Path.of(original).getFileName().toString();
    }

    private com.dulcepack.backend.catalog.domain.ProductCategoryEntity findCategory(String value) {
        String slug = slugify(value);
        return categories.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY", "Category is invalid"));
    }

    private ProductFlavor parseFlavor(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return ProductFlavor.valueOf(CatalogService.normalizeSearchText(value).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FLAVOR", "Flavor is invalid");
        }
    }

    private AdminProductResponse toResponse(ProductEntity product) {
        var productInventory = product.getInventory();
        ProductImageResponse image = product.getImage() == null ? null
                : new ProductImageResponse(product.getImage().getFileName(), product.getImage().getContentType(),
                        "/api/catalog/products/" + product.getSlug() + "/image");
        String flavor = product.getFlavor() == null ? null
                : product.getFlavor().name().toLowerCase(Locale.ROOT);
        return new AdminProductResponse(product.getSlug(), product.getName(), product.getShortName(),
                product.getDescription(), product.getPrice(), product.getCurrency(), product.getCategory().getName(),
                parseTags(product.getTags()), flavor, productInventory.getAvailable(), productInventory.getMinimumStock(),
                productInventory.getStatus().name(), productInventory.getVersion(), image, product.getUpdatedAt());
    }

    private String searchText(String name, String description) {
        return CatalogService.normalizeSearchText(name + " " + description);
    }

    private String shortName(String name) {
        return name.length() <= 80 ? name : name.substring(0, 80);
    }

    private String serializeTags(List<String> tags) {
        return tags.stream().map(String::trim).collect(Collectors.joining(","));
    }

    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of();
        }
        return Arrays.stream(tags.split(",")).map(String::trim).filter(StringUtils::hasText).toList();
    }

    private String slugify(String value) {
        return CatalogService.normalizeSearchText(value)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? CatalogService.normalizeSearchText(value) : null;
    }

    public record ProductImageContent(String fileName, String contentType, byte[] content) {
        public ProductImageContent {
            content = content.clone();
        }

        @Override
        public byte[] content() {
            return content.clone();
        }
    }
}
