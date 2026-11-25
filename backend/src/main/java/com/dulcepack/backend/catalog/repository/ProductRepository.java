package com.dulcepack.backend.catalog.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import com.dulcepack.backend.catalog.domain.ProductEntity;
import com.dulcepack.backend.catalog.domain.ProductFlavor;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    @EntityGraph(attributePaths = {"category", "inventory", "image"})
    @Query("""
            select p from ProductEntity p
            where (:query is null or p.searchText like concat('%', :query, '%'))
              and (:category is null or p.category.slug = :category)
              and (:flavor is null or p.flavor = :flavor)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            order by p.name
            """)
    List<ProductEntity> search(@Param("query") String query,
            @Param("category") String category,
            @Param("flavor") ProductFlavor flavor,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    @EntityGraph(attributePaths = {"category", "inventory", "image"})
    Optional<ProductEntity> findBySlug(String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"category", "inventory", "image"})
    @Query("select p from ProductEntity p where p.slug = :slug")
    Optional<ProductEntity> findBySlugForUpdate(@Param("slug") String slug);

    boolean existsBySlug(String slug);
}
