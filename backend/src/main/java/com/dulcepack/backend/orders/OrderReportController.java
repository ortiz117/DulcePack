package com.dulcepack.backend.orders;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports/orders")
@PreAuthorize("hasRole('ADMIN')")
class OrderReportController {

    private final OrderService orderService;

    OrderReportController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/summary")
    OrderReportResponse summary() {
        return orderService.summarize();
    }
}
