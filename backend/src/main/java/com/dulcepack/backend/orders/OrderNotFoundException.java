package com.dulcepack.backend.orders;

import com.dulcepack.backend.shared.error.NotFoundException;

class OrderNotFoundException extends NotFoundException {

    OrderNotFoundException(String code) {
        super("ORDER_NOT_FOUND", "Order not found: " + code);
    }
}
