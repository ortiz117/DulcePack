package com.dulcepack.backend.orders;

import java.util.List;

import org.springframework.data.domain.Page;

public record OrderPageResponse(
    List<OrderResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    static OrderPageResponse from(Page<OrderResponse> orders) {
        return new OrderPageResponse(
            orders.getContent(),
            orders.getNumber(),
            orders.getSize(),
            orders.getTotalElements(),
            orders.getTotalPages()
        );
    }
}
