package com.dulcepack.backend.catalog.api;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertProductRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 1000) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotNull @Min(0) Integer stock,
        @Min(0) Integer minimumStock,
        @NotBlank @Size(max = 50) String category,
        @NotNull @Size(max = 20) List<@NotBlank @Size(max = 50) String> tags,
        @Size(max = 20) String flavor) {
}
