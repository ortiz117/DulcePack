package com.dulcepack.backend.orders;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
class OrderNotFoundException extends RuntimeException {

    OrderNotFoundException(String code) {
        super("Order not found: " + code);
    }
}
