package com.dulcepack.backend.cart.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddCartItemRequest(
        @NotBlank @Size(max = 100) String productSlug,
        @NotNull @Min(1) @Max(99) Integer quantity) {
}
