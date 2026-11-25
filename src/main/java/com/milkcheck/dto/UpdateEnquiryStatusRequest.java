package com.milkcheck.dto;

import com.milkcheck.enums.EnquiryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEnquiryStatusRequest(
        @NotNull(message = "Status is required") EnquiryStatus status) {}