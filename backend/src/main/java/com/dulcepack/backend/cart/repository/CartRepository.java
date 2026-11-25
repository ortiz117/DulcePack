package com.dulcepack.backend.cart.repository;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dulcepack.backend.cart.domain.CartEntity;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {
    Optional<CartEntity> findByTokenHash(String tokenHash);

    void deleteByExpiresAtBefore(Instant instant);
}
