package com.dulcepack.backend.orders;

import java.math.BigDecimal;
import java.util.Map;

public record OrderReportResponse(
    long totalOrders,
    BigDecimal grossRevenue,
    BigDecimal averageTicket,
    Map<OrderStatus, Long> ordersByStatus
) {
}
