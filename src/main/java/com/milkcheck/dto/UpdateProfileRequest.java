package com.milkcheck.dto;

import jakarta.validation.constraints.*;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required") @Size(max = 100) String fullName,
        @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Enter a valid phone number") String phone,
        @Size(max = 255) String addressLine,
        @Size(max = 80) String city,
        @Size(max = 80) String state,
        @Size(max = 15) String postalCode) {}