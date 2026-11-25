package com.dulcepack.backend.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.EnumMap;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class OrderService {

    private final OrderRepository orderRepository;
    private final ProductCatalog productCatalog;
    private final Clock clock = Clock.systemUTC();

    private static final BigDecimal SHIPPING = new BigDecimal("79.00");
    private static final BigDecimal DULCE50_DISCOUNT = new BigDecimal("50.00");

    OrderService(OrderRepository orderRepository, ProductCatalog productCatalog) {
        this.orderRepository = orderRepository;
        this.productCatalog = productCatalog;
    }

    @Transactional
    OrderResponse create(CreateOrderRequest request, String ownerSubject) {
        if (ownerSubject == null || ownerSubject.length() > 254) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authenticated subject");
        }
        var resolvedItems = request.items().stream()
            .map(item -> new ResolvedItem(productCatalog.findRequired(item.productId().trim()), item.quantity()))
            .toList();
        var subtotal = resolvedItems.stream()
            .map(item -> money(item.product().unitPrice()).multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        var shipping = SHIPPING;
        var discount = discountFor(request.couponCode(), subtotal.add(shipping));
        var total = subtotal.add(shipping).subtract(discount);

        var order = new OrderEntity(
            ownerSubject,
            request.customerName().trim(),
            request.email().trim().toLowerCase(),
            request.deliveryAddress().trim(),
            subtotal,
            shipping,
            discount,
            total,
            clock.instant()
        );
        resolvedItems.forEach(item -> order.addItem(new OrderItemEntity(
            item.product().id(),
            item.product().name(),
            item.quantity(),
            money(item.product().unitPrice())
        )));

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    OrderResponse findByCode(String code, String subject, boolean administrator) {
        var order = orderRepository.findById(parseId(code))
            .orElseThrow(() -> new OrderNotFoundException(code));
        if (!administrator && !order.getOwnerSubject().equals(subject)) {
            throw new OrderNotFoundException(code);
        }
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    OrderPageResponse list(Pageable pageable) {
        var sorted = pageable.getSort().isSorted()
            ? pageable
            : org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
            );
        return OrderPageResponse.from(orderRepository.findAll(sorted).map(OrderResponse::from));
    }

    @Transactional
    OrderResponse updateStatus(String code, OrderStatus nextStatus) {
        var order = orderRepository.findById(parseId(code))
            .orElseThrow(() -> new OrderNotFoundException(code));
        order.changeStatus(nextStatus);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    OrderReportResponse summarize() {
        var orders = orderRepository.findAll();
        var ordersByStatus = new EnumMap<OrderStatus, Long>(OrderStatus.class);
        for (var status : OrderStatus.values()) {
            ordersByStatus.put(status, 0L);
        }
        orders.forEach(order -> ordersByStatus.merge(order.getStatus(), 1L, Long::sum));

        var billableOrders = orders.stream()
            .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
            .toList();
        var grossRevenue = billableOrders.stream()
            .map(OrderEntity::getTotal)
            .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        var averageTicket = billableOrders.isEmpty()
            ? BigDecimal.ZERO.setScale(2)
            : grossRevenue.divide(BigDecimal.valueOf(billableOrders.size()), 2, RoundingMode.HALF_UP);

        return new OrderReportResponse(orders.size(), grossRevenue, averageTicket, ordersByStatus);
    }

    private static long parseId(String code) {
        if (code == null || !code.matches("DP-\\d{6,}")) {
            throw new OrderNotFoundException(code);
        }
        try {
            return Long.parseLong(code.substring(3));
        } catch (NumberFormatException exception) {
            throw new OrderNotFoundException(code);
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal discountFor(String couponCode, BigDecimal payableBeforeDiscount) {
        if (couponCode == null || couponCode.isBlank()) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (!couponCode.trim().equalsIgnoreCase("DULCE50")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown coupon");
        }
        return DULCE50_DISCOUNT.min(payableBeforeDiscount);
    }

    private record ResolvedItem(ProductCatalog.ProductSnapshot product, int quantity) {
    }
}
