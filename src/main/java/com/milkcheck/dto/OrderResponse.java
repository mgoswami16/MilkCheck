package com.milkcheck.dto;

import com.milkcheck.entity.Order;
import com.milkcheck.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        String contactPhone,
        String customerName,
        String customerEmail,
        String paymentMethod,
        String paymentStatus,
        LocalDateTime createdAt,
        List<OrderItemResponse> items) {

    public static OrderResponse from(Order o) {
        var pay = o.getPayment();
        return new OrderResponse(o.getId(), o.getOrderNumber(), o.getStatus(), o.getTotalAmount(),
                o.getShippingAddress(), o.getContactPhone(),
                o.getUser().getFullName(), o.getUser().getEmail(),
                pay != null ? pay.getMethod().name() : null,
                pay != null ? pay.getStatus().name() : null,
                o.getCreatedAt(),
                o.getItems().stream().map(OrderItemResponse::from).toList());
    }
}