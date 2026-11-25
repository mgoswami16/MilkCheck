package com.milkcheck.service;

import com.milkcheck.dto.CheckoutRequest;
import com.milkcheck.dto.OrderResponse;
import com.milkcheck.dto.PageResponse;
import com.milkcheck.entity.*;
import com.milkcheck.enums.OrderStatus;
import com.milkcheck.enums.PaymentMethod;
import com.milkcheck.enums.PaymentStatus;
import com.milkcheck.exception.BadRequestException;
import com.milkcheck.exception.InsufficientStockException;
import com.milkcheck.exception.ResourceNotFoundException;
import com.milkcheck.repository.CartRepository;
import com.milkcheck.repository.OrderRepository;
import com.milkcheck.repository.ProductRepository;
import com.milkcheck.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.milkcheck.enums.OrderStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    /** Allowed status transitions. Terminal states have no exits. */
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            PENDING,    EnumSet.of(CONFIRMED, CANCELLED),
            CONFIRMED,  EnumSet.of(PROCESSING, CANCELLED),
            PROCESSING, EnumSet.of(SHIPPED, CANCELLED),
            SHIPPED,    EnumSet.of(DELIVERED),
            DELIVERED,  EnumSet.noneOf(OrderStatus.class),
            CANCELLED,  EnumSet.noneOf(OrderStatus.class));

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    // ---------- Customer ----------

    @Transactional
    public OrderResponse checkout(String email, CheckoutRequest req) {
        User user = userService.getEntityByEmail(email);
        Cart cart = cartRepository.findByUserId(user.getId())
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setShippingAddress(req.shippingAddress().trim());
        order.setContactPhone(req.contactPhone().trim());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (!p.isActive()) {
                throw new BadRequestException("'" + p.getName() + "' is no longer available. Please remove it from your cart.");
            }
            // Atomic: only succeeds if enough stock remains.
            if (productRepository.decrementStock(p.getId(), ci.getQuantity()) == 0) {
                throw new InsufficientStockException(p.getName(), p.getStockQuantity());
            }

            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setProductName(p.getName());
            oi.setUnitPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            order.addItem(oi);
            total = total.add(oi.getSubtotal());
        }

        order.setTotalAmount(total);
        order.setPayment(buildPayment(order, req.paymentMethod(), total));

        Order saved = orderRepository.save(order);
        cart.getItems().clear();

        log.info("Order placed: {} user={} total={} method={}",
                saved.getOrderNumber(), email, total, req.paymentMethod());
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {
        User user = userService.getEntityByEmail(email);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String email, Long orderId) {
        User user = userService.getEntityByEmail(email);
        return orderRepository.findByIdAndUserId(orderId, user.getId())
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    @Transactional
    public OrderResponse cancelMyOrder(String email, Long orderId) {
        User user = userService.getEntityByEmail(email);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != PENDING && order.getStatus() != CONFIRMED) {
            throw new BadRequestException("This order can no longer be cancelled");
        }
        applyStatus(order, CANCELLED);
        return OrderResponse.from(order);
    }

    // ---------- Admin ----------

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listAll(OrderStatus status, int page, int size) {
        Pageable pageable = PageUtil.of(page, size,
                Sort.by("createdAt").descending().and(Sort.by("id").descending()));
        Page<Order> result = (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PageResponse.from(result.map(OrderResponse::from));
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus next) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        applyStatus(order, next);
        log.info("Order {} status -> {}", order.getOrderNumber(), next);
        return OrderResponse.from(order);
    }

    // ---------- Helpers ----------

    private void applyStatus(Order order, OrderStatus next) {
        OrderStatus current = order.getStatus();
        if (!TRANSITIONS.get(current).contains(next)) {
            throw new BadRequestException("Cannot change order status from " + current + " to " + next);
        }

        Payment payment = order.getPayment();

        if (next == CANCELLED) {
            order.getItems().stream()
                    .filter(i -> i.getProduct() != null)
                    .forEach(i -> productRepository.incrementStock(i.getProduct().getId(), i.getQuantity()));
            if (payment != null) {
                payment.setStatus(payment.getStatus() == PaymentStatus.PAID
                        ? PaymentStatus.REFUNDED
                        : PaymentStatus.FAILED);           // payment was never collected
            }
        }

        if (next == DELIVERED && payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.PAID);         // cash collected on delivery
            payment.setPaidAt(LocalDateTime.now());
        }

        order.setStatus(next);
    }

    private Payment buildPayment(Order order, PaymentMethod method, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setMethod(method);

        if (method == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.PENDING);
        } else {
            // SIMULATED gateway: replace with a real payment provider integration.
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionRef("SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        }
        return payment;
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "MC-" + date + "-" + suffix;
    }
}