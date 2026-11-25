package com.milkcheck.controller;

import com.milkcheck.dto.AddToCartRequest;
import com.milkcheck.dto.CartResponse;
import com.milkcheck.dto.UpdateCartItemRequest;
import com.milkcheck.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserDetails principal) {
        return cartService.getCart(principal.getUsername());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(@AuthenticationPrincipal UserDetails principal,
                                @Valid @RequestBody AddToCartRequest request) {
        return cartService.addItem(principal.getUsername(), request);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(@AuthenticationPrincipal UserDetails principal,
                                   @PathVariable Long itemId,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(principal.getUsername(), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@AuthenticationPrincipal UserDetails principal,
                                   @PathVariable Long itemId) {
        return cartService.removeItem(principal.getUsername(), itemId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal UserDetails principal) {
        cartService.clear(principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}