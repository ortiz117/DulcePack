package com.dulcepack.backend.catalog.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dulcepack.backend.catalog.service.AdminCatalogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final AdminCatalogService catalog;

    public AdminProductController(AdminCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    List<AdminProductResponse> list(@RequestParam(required = false) String query) {
        return catalog.list(query);
    }

    @GetMapping("/{slug}")
    AdminProductResponse detail(@PathVariable String slug) {
        return catalog.detail(slug);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<AdminProductResponse> create(
            @Valid @RequestPart("product") UpsertProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        AdminProductResponse created = catalog.create(request, image);
        return ResponseEntity.created(URI.create("/api/admin/products/" + created.id())).body(created);
    }

    @PutMapping(path = "/{slug}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    AdminProductResponse update(@PathVariable String slug,
            @Valid @RequestPart("product") UpsertProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return catalog.update(slug, request, image);
    }
}
