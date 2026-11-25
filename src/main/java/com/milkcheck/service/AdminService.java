package com.milkcheck.service;

import com.milkcheck.dto.DashboardResponse;
import com.milkcheck.dto.OrderResponse;
import com.milkcheck.dto.PageResponse;
import com.milkcheck.dto.UserResponse;
import com.milkcheck.entity.Role;
import com.milkcheck.entity.User;
import com.milkcheck.enums.OrderStatus;
import com.milkcheck.exception.BadRequestException;
import com.milkcheck.exception.ResourceNotFoundException;
import com.milkcheck.repository.OrderRepository;
import com.milkcheck.repository.ProductRepository;
import com.milkcheck.repository.UserRepository;
import com.milkcheck.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            byStatus.put(s.name(), orderRepository.countByStatus(s));
        }
        List<OrderResponse> recent = orderRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(OrderResponse::from)
                .toList();

        return new DashboardResponse(
                productRepository.countByActiveTrue(),
                orderRepository.count(),
                userRepository.countCustomers(),
                orderRepository.sumRevenueExcluding(OrderStatus.CANCELLED),
                byStatus.get(OrderStatus.PENDING.name()),
                byStatus,
                recent);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listCustomers(String keyword, int page, int size) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return PageResponse.from(
                userRepository.searchCustomers(kw, PageUtil.of(page, size, Sort.by("createdAt").descending()))
                        .map(UserResponse::from));
    }

    @Transactional
    public UserResponse setCustomerEnabled(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        if (user.getRoles().stream().map(Role::getName).anyMatch("ROLE_ADMIN"::equals)) {
            throw new BadRequestException("Admin accounts cannot be modified here");
        }
        user.setEnabled(enabled);
        log.info("Customer {} {}", user.getEmail(), enabled ? "enabled" : "disabled");
        return UserResponse.from(user);
    }
}