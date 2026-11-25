package com.milkcheck.controller;

import com.milkcheck.dto.PageResponse;
import com.milkcheck.dto.ProductResponse;
import com.milkcheck.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** sort: newest (default) | price_asc | price_desc | name */
    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return productService.search(keyword, categoryId, minPrice, maxPrice, inStockOnly, sort, page, size);
    }

    @GetMapping("/featured")
    public List<ProductResponse> featured() {
        return productService.featured();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getActive(id);
    }
}