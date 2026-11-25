package com.milkcheck.dto;

import com.milkcheck.entity.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        boolean inStock,
        String imageUrl,
        String unit,
        boolean featured,
        boolean active,
        Long categoryId,
        String categoryName) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getStockQuantity(), p.getStockQuantity() > 0, p.getImageUrl(), p.getUnit(),
                p.isFeatured(), p.isActive(),
                p.getCategory().getId(), p.getCategory().getName());
    }
}