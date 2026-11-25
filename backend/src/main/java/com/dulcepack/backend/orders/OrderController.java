package com.dulcepack.backend.orders;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Validated
class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    ResponseEntity<OrderResponse> create(
        @Valid @RequestBody CreateOrderRequest request,
        Authentication authentication
    ) {
        var order = orderService.create(request, authentication.getName());
        return ResponseEntity.created(URI.create("/api/orders/" + order.code())).body(order);
    }

    @GetMapping("/{code}")
    OrderResponse findByCode(@PathVariable String code, Authentication authentication) {
        boolean administrator = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return orderService.findByCode(code, authentication.getName(), administrator);
    }
}
