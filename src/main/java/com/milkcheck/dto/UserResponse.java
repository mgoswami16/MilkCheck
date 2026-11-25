package com.milkcheck.dto;

import com.milkcheck.entity.Role;
import com.milkcheck.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String addressLine,
        String city,
        String state,
        String postalCode,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getAddressLine(), u.getCity(), u.getState(), u.getPostalCode(), u.isEnabled(),
                u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                u.getCreatedAt());
    }
}