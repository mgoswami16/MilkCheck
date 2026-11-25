package com.milkcheck.dto;

import com.milkcheck.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull(message = "Status is required") OrderStatus status) {}