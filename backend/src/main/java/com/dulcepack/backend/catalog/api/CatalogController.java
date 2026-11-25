package com.dulcepack.backend.catalog.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.dulcepack.backend.catalog.service.CatalogService;
import com.dulcepack.backend.catalog.service.AdminCatalogService;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogService catalog;
    private final AdminCatalogService adminCatalog;

    public CatalogController(CatalogService catalog, AdminCatalogService adminCatalog) {
        this.catalog = catalog;
        this.adminCatalog = adminCatalog;
    }

    @GetMapping("/categories")
    List<CategoryResponse> categories() {
        return catalog.categories();
    }

    @GetMapping("/products")
    List<CatalogProductResponse> products(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String flavor,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return catalog.search(query, category, flavor, minPrice, maxPrice);
    }

    @GetMapping("/products/page")
    CatalogPagedResponse<CatalogProductResponse> productsPage(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String flavor,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return catalog.searchPage(query, category, flavor, minPrice, maxPrice, page, size);
    }

    @GetMapping("/products/{slug}")
    CatalogProductResponse product(@PathVariable String slug) {
        return catalog.detail(slug);
    }

    @GetMapping("/products/{slug}/image")
    ResponseEntity<byte[]> image(@PathVariable String slug) {
        var image = adminCatalog.image(slug);
        String fileName = image.fileName().replace("\"", "");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "sandbox")
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .body(image.content());
    }
}
