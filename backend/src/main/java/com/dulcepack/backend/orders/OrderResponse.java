package com.dulcepack.backend.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    String code,
    String customerName,
    String email,
    String deliveryAddress,
    List<Item> items,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal discount,
    BigDecimal total,
    OrderStatus status,
    Instant createdAt
) {
    static OrderResponse from(OrderEntity order) {
        return new OrderResponse(
            order.getCode(),
            order.getCustomerName(),
            order.getEmail(),
            order.getDeliveryAddress(),
            order.getItems().stream().map(Item::from).toList(),
            order.getSubtotal(),
            order.getShipping(),
            order.getDiscount(),
            order.getTotal(),
            order.getStatus(),
            order.getCreatedAt()
        );
    }

    public record Item(
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
    ) {
        static Item from(OrderItemEntity item) {
            return new Item(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }
    }
}
