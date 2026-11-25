package com.dulcepack.backend.catalog.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dulcepack.backend.catalog.domain.ProductInventoryEntity;

public interface ProductInventoryRepository extends JpaRepository<ProductInventoryEntity, UUID> {
}
