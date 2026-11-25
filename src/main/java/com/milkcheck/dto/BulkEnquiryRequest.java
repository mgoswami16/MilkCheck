package com.milkcheck.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record BulkEnquiryRequest(
        @NotBlank(message = "Contact name is required")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "Business name is required")
        @Size(max = 150)
        String businessName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Enter a valid phone number")
        String phone,

        @NotBlank(message = "Business type is required")
        @Size(max = 50)
        String businessType,

        @NotBlank(message = "Delivery city is required")
        @Size(max = 120)
        String city,

        
        List<String> products,

        @NotBlank(message = "Estimated quantity is required")
        @Size(max = 100)
        String estimatedQuantity,

        @NotBlank(message = "Frequency is required")
        @Size(max = 40)
        String frequency,

        @Size(max = 300)
        String deliveryAddress,

        @NotBlank(message = "Please describe your requirement")
        @Size(max = 1500)
        String message) {}