package com.milkcheck.controller;

import com.milkcheck.dto.DashboardResponse;
import com.milkcheck.dto.PageResponse;
import com.milkcheck.dto.UpdateCustomerStatusRequest;
import com.milkcheck.dto.UserResponse;
import com.milkcheck.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return adminService.getDashboard();
    }

    @GetMapping("/customers")
    public PageResponse<UserResponse> customers(@RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return adminService.listCustomers(keyword, page, size);
    }

    @PatchMapping("/customers/{id}/status")
    public UserResponse setStatus(@PathVariable Long id,
                                  @RequestBody UpdateCustomerStatusRequest request) {
        return adminService.setCustomerEnabled(id, request.enabled());
    }
}