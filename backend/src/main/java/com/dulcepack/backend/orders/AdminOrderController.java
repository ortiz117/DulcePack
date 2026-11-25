package com.dulcepack.backend.orders;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
class AdminOrderController {

    private final OrderService orderService;

    AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    OrderPageResponse list(Pageable pageable) {
        return orderService.list(pageable);
    }

    @PatchMapping("/{code}/status")
    OrderResponse updateStatus(
        @PathVariable String code,
        @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateStatus(code, request.status());
    }
}
