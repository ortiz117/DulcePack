package com.dulcepack.backend.cart.repository;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dulcepack.backend.cart.domain.CartItemEntity;

import jakarta.persistence.LockModeType;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {

    @EntityGraph(attributePaths = {"product", "product.category", "product.inventory", "product.image"})
    @Query("select item from CartItemEntity item where item.cart.id = :cartId order by item.product.name")
    List<CartItemEntity> findByCartId(@Param("cartId") UUID cartId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from CartItemEntity item where item.cart.id = :cartId and item.product.slug = :slug")
    java.util.Optional<CartItemEntity> findByCartIdAndProductSlugForUpdate(
            @Param("cartId") UUID cartId, @Param("slug") String slug);


    @Query("select coalesce(sum(item.quantity), 0) from CartItemEntity item "
            + "where item.product.slug = :slug and item.cart.expiresAt > :now "
            + "and item.cart.updatedAt > :cutoff")
    int reservedQuantity(@Param("slug") String slug, @Param("now") Instant now, @Param("cutoff") Instant cutoff);
}
