package com.milkcheck.service;

import com.milkcheck.dto.*;
import com.milkcheck.entity.Cart;
import com.milkcheck.entity.CartItem;
import com.milkcheck.entity.Product;
import com.milkcheck.entity.User;
import com.milkcheck.exception.InsufficientStockException;
import com.milkcheck.exception.ResourceNotFoundException;
import com.milkcheck.repository.CartItemRepository;
import com.milkcheck.repository.CartRepository;
import com.milkcheck.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Transactional
    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        // Drop items whose product was deactivated by an admin.
        cart.getItems().removeIf(i -> !i.getProduct().isActive());
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String email, AddToCartRequest req) {
        Cart cart = getOrCreateCart(email);
        Product product = productRepository.findByIdAndActiveTrue(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.productId()));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst();

        int newQuantity = existing.map(CartItem::getQuantity).orElse(0) + req.quantity();
        ensureStock(product, newQuantity);

        if (existing.isPresent()) {
            existing.get().setQuantity(newQuantity);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(newQuantity);
            cart.getItems().add(cartItemRepository.save(item));
        }
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(String email, Long itemId, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(email);
        CartItem item = findItem(cart, itemId);
        ensureStock(item.getProduct(), req.quantity());
        item.setQuantity(req.quantity());
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(String email, Long itemId) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().remove(findItem(cart, itemId));
        return toResponse(cart);
    }

    @Transactional
    public void clear(String email) {
        getOrCreateCart(email).getItems().clear();
    }

    // ---------- Helpers ----------

    private Cart getOrCreateCart(String email) {
        User user = userService.getEntityByEmail(email);
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));
    }

    private void ensureStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName(), product.getStockQuantity());
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getId))
                .map(CartItemResponse::from)
                .toList();
        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, totalItems, total);
    }
}