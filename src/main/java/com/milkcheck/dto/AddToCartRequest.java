package com.milkcheck.dto;

import jakarta.validation.constraints.*;

public record AddToCartRequest(
        @NotNull(message = "Product is required") Long productId,
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100, message = "Quantity cannot exceed 100") int quantity) {}