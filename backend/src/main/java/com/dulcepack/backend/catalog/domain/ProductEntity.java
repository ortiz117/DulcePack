package com.dulcepack.backend.catalog.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "short_name", nullable = false, length = 80)
    private String shortName;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "search_text", nullable = false, length = 1200)
    private String searchText;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategoryEntity category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductFlavor flavor;

    @Column(nullable = false, length = 1000)
    private String tags;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY, optional = false)
    private ProductInventoryEntity inventory;

    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    private ProductImageEntity image;

    protected ProductEntity() {
    }

    public ProductEntity(UUID id, String slug, String name, String shortName, String description,
            String searchText, BigDecimal price, ProductCategoryEntity category, ProductFlavor flavor,
            String tags, Instant now) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.searchText = searchText;
        this.price = price;
        this.currency = "MXN";
        this.category = category;
        this.flavor = flavor;
        this.tags = tags;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public ProductCategoryEntity getCategory() { return category; }
    public ProductFlavor getFlavor() { return flavor; }
    public String getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public ProductInventoryEntity getInventory() { return inventory; }
    public ProductImageEntity getImage() { return image; }

    public void attachInventory(ProductInventoryEntity inventory) {
        this.inventory = inventory;
    }

    public void attachImage(ProductImageEntity image) {
        this.image = image;
    }

    public void update(String name, String shortName, String description, String searchText,
            BigDecimal price, ProductCategoryEntity category, ProductFlavor flavor, String tags, Instant now) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.searchText = searchText;
        this.price = price;
        this.category = category;
        this.flavor = flavor;
        this.tags = tags;
        this.updatedAt = now;
    }
}
