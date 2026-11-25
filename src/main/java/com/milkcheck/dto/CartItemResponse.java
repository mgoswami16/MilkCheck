package com.milkcheck.dto;

import com.milkcheck.entity.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String imageUrl,
        String unit,
        BigDecimal unitPrice,
        int quantity,
        int availableStock,
        BigDecimal subtotal) {

    public static CartItemResponse from(CartItem ci) {
        var p = ci.getProduct();
        return new CartItemResponse(ci.getId(), p.getId(), p.getName(), p.getImageUrl(), p.getUnit(),
                p.getPrice(), ci.getQuantity(), p.getStockQuantity(),
                p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
    }
}