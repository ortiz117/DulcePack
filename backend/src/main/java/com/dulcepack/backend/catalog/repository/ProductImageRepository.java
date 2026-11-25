package com.dulcepack.backend.catalog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dulcepack.backend.catalog.domain.ProductImageEntity;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, UUID> {
    Optional<ProductImageEntity> findByProductSlug(String slug);

    Optional<ProductImageEntity> findByProductId(UUID productId);
}
