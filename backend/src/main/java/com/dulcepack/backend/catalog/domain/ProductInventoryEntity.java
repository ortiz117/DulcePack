package com.dulcepack.backend.catalog.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "product_inventory")
public class ProductInventoryEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(nullable = false)
    private int available;

    @Column(name = "minimum_stock", nullable = false)
    private int minimumStock;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductInventoryEntity() {
    }

    public ProductInventoryEntity(ProductEntity product, int available, int minimumStock, Instant now) {
        this.product = product;
        this.available = available;
        this.minimumStock = minimumStock;
        this.updatedAt = now;
    }

    public UUID getProductId() { return productId; }
    public ProductEntity getProduct() { return product; }
    public int getAvailable() { return available; }
    public int getMinimumStock() { return minimumStock; }
    public long getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }
    public StockStatus getStatus() { return StockStatus.from(available, minimumStock); }

    public void setAvailable(int available, Instant now) {
        this.available = available;
        this.updatedAt = now;
    }
}
