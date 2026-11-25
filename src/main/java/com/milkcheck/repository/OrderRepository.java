package com.milkcheck.repository;

import com.milkcheck.entity.Order;
import com.milkcheck.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findTop5ByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    /** Revenue = sum of all orders excluding the given status (CANCELLED). */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status <> :excluded")
    BigDecimal sumRevenueExcluding(@Param("excluded") OrderStatus excluded);
}