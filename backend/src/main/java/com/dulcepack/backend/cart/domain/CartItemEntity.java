package com.dulcepack.backend.cart.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_items")
public class CartItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private com.dulcepack.backend.catalog.domain.ProductEntity product;

    @Column(nullable = false)
    private int quantity;


    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CartItemEntity() {
    }

    public CartItemEntity(UUID id, CartEntity cart,
            com.dulcepack.backend.catalog.domain.ProductEntity product, int quantity, Instant now) {
        this.id = id;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }

    public com.dulcepack.backend.catalog.domain.ProductEntity getProduct() { return product; }
    public int getQuantity() { return quantity; }


    public void increase(int amount, Instant now) {
        this.quantity += amount;
        this.updatedAt = now;
    }

    public void setQuantity(int quantity, Instant now) {
        this.quantity = quantity;
        this.updatedAt = now;
    }
}
