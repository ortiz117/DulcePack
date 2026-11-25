package com.dulcepack.backend.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dulcepack.backend.cart.domain.PromotionEntity;

public interface PromotionRepository extends JpaRepository<PromotionEntity, String> {
    Optional<PromotionEntity> findByCodeAndActiveTrue(String code);
}
