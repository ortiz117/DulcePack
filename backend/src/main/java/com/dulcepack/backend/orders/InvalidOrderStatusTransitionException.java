package com.dulcepack.backend.orders;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
class InvalidOrderStatusTransitionException extends RuntimeException {

    InvalidOrderStatusTransitionException(OrderStatus current, OrderStatus next) {
        super("Cannot change order status from %s to %s".formatted(current, next));
    }
}
