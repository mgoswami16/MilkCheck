package com.milkcheck.controller;

import com.milkcheck.dto.CheckoutRequest;
import com.milkcheck.dto.OrderResponse;
import com.milkcheck.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@AuthenticationPrincipal UserDetails principal,
                                  @Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(principal.getUsername(), request);
    }

    @GetMapping
    public List<OrderResponse> myOrders(@AuthenticationPrincipal UserDetails principal) {
        return orderService.getMyOrders(principal.getUsername());
    }

    @GetMapping("/{id}")
    public OrderResponse myOrder(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        return orderService.getMyOrder(principal.getUsername(), id);
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        return orderService.cancelMyOrder(principal.getUsername(), id);
    }
}