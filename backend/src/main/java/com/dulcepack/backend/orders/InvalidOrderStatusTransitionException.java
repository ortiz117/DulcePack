package com.dulcepack.backend.orders;

import com.dulcepack.backend.shared.error.ConflictException;

class InvalidOrderStatusTransitionException extends ConflictException {

    InvalidOrderStatusTransitionException(OrderStatus current, OrderStatus next) {
        super("INVALID_ORDER_STATUS_TRANSITION",
            "Cannot change order status from %s to %s".formatted(current, next));
    }
}
