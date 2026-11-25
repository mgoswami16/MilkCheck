package com.milkcheck.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank(message = "Product name is required") @Size(max = 150) String name,
        @Size(max = 2000, message = "Description must be at most 2000 characters") String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stockQuantity,

        @Size(max = 255) String imageUrl,
        @Size(max = 30) String unit,
        boolean featured,
        Boolean active,

        

        @NotNull(message = "Category is required") Long categoryId) {}