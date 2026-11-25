package com.milkcheck.dto;

import com.milkcheck.entity.BulkEnquiry;
import com.milkcheck.enums.EnquiryStatus;

import java.time.LocalDateTime;

public record BulkEnquiryResponse(
        Long id,
        String refNumber,
        String fullName,
        String businessName,
        String email,
        String phone,
        String businessType,
        String city,
        String products,
        String estimatedQuantity,
        String frequency,
        String deliveryAddress,
        String message,
        EnquiryStatus status,
        LocalDateTime createdAt) {

    public static BulkEnquiryResponse from(BulkEnquiry e) {
        return new BulkEnquiryResponse(
                e.getId(),
                e.getRefNumber(),
                e.getFullName(),
                e.getBusinessName(),
                e.getEmail(),
                e.getPhone(),
                e.getBusinessType(),
                e.getCity(),
                e.getProducts(),
                e.getEstimatedQuantity(),
                e.getFrequency(),
                e.getDeliveryAddress(),
                e.getMessage(),
                e.getStatus(),
                e.getCreatedAt());
    }
}