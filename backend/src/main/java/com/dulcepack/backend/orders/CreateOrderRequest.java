package com.dulcepack.backend.orders;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
    @NotBlank @Size(max = 140) String customerName,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 300) String deliveryAddress,
    @Size(max = 50) String couponCode,
    @NotEmpty @Size(max = 100) List<@Valid Item> items
) {
    public record Item(
        @NotBlank @Size(max = 100) String productId,
        @Positive @Max(100_000) int quantity
    ) {
    }
}
