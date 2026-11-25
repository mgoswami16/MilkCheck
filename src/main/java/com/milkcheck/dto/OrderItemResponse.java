package com.milkcheck.dto;

import com.milkcheck.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal) {

    public static OrderItemResponse from(OrderItem oi) {
        return new OrderItemResponse(
                oi.getProduct() != null ? oi.getProduct().getId() : null,
                oi.getProductName(), oi.getUnitPrice(), oi.getQuantity(), oi.getSubtotal());
    }
}