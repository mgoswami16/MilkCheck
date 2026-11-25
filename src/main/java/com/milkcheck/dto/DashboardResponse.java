package com.milkcheck.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalProducts,
        long totalOrders,
        long totalCustomers,
        BigDecimal totalRevenue,
        long pendingOrders,
        Map<String, Long> ordersByStatus,
        List<OrderResponse> recentOrders) {}