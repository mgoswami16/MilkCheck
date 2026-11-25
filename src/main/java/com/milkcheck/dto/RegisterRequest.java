package com.milkcheck.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be 8-64 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                 message = "Password must contain both letters and numbers")
        String password,

        @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Enter a valid phone number")
        String phone) {}