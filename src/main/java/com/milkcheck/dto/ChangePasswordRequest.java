package com.milkcheck.dto;

import jakarta.validation.constraints.*;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required") String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 64, message = "Password must be 8-64 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                 message = "Password must contain both letters and numbers")
        String newPassword) {}