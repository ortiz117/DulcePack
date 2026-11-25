package com.dulcepack.backend.catalog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dulcepack.backend.catalog.domain.ProductCategoryEntity;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, UUID> {
    Optional<ProductCategoryEntity> findBySlug(String slug);
}
