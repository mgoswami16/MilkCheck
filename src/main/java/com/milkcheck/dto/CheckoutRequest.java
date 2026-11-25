package com.milkcheck.dto;

import com.milkcheck.enums.PaymentMethod;
import jakarta.validation.constraints.*;

public record CheckoutRequest(
        @NotBlank(message = "Shipping address is required")
        @Size(max = 500, message = "Address is too long")
        String shippingAddress,

        @NotBlank(message = "Contact phone is required")
        @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Enter a valid phone number")
        String contactPhone,

        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod) {}