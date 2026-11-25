package com.milkcheck.dto;

import com.milkcheck.entity.Category;

public record CategoryResponse(Long id, String name, String description, String imageUrl) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getImageUrl());
    }
}