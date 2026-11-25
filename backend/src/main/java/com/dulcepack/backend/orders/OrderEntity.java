package com.dulcepack.backend.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "owner_subject", nullable = false, length = 254)
    private String ownerSubject;

    @Column(name = "customer_name", nullable = false, length = 140)
    private String customerName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "delivery_address", nullable = false, length = 300)
    private String deliveryAddress;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shipping;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {
    }

    OrderEntity(
        String ownerSubject,
        String customerName,
        String email,
        String deliveryAddress,
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal discount,
        BigDecimal total,
        Instant createdAt
    ) {
        this.ownerSubject = ownerSubject;
        this.customerName = customerName;
        this.email = email;
        this.deliveryAddress = deliveryAddress;
        this.subtotal = subtotal;
        this.shipping = shipping;
        this.discount = discount;
        this.total = total;
        this.status = OrderStatus.PENDING;
        this.createdAt = createdAt;
    }

    void addItem(OrderItemEntity item) {
        item.attachTo(this);
        items.add(item);
    }

    void changeStatus(OrderStatus nextStatus) {
        var allowed = switch (status) {
            case PENDING -> Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED);
            case PREPARING -> Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
            case SHIPPED -> Set.of(OrderStatus.DELIVERED);
            case DELIVERED, CANCELLED -> Set.<OrderStatus>of();
        };
        if (!allowed.contains(nextStatus)) {
            throw new InvalidOrderStatusTransitionException(status, nextStatus);
        }
        status = nextStatus;
    }

    Long getId() {
        return id;
    }

    String getOwnerSubject() {
        return ownerSubject;
    }

    String getCode() {
        return "DP-%06d".formatted(id);
    }

    String getCustomerName() {
        return customerName;
    }

    String getEmail() {
        return email;
    }

    String getDeliveryAddress() {
        return deliveryAddress;
    }

    BigDecimal getSubtotal() {
        return subtotal;
    }

    BigDecimal getShipping() {
        return shipping;
    }

    BigDecimal getDiscount() {
        return discount;
    }

    BigDecimal getTotal() {
        return total;
    }

    OrderStatus getStatus() {
        return status;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    List<OrderItemEntity> getItems() {
        return Collections.unmodifiableList(items);
    }
}
