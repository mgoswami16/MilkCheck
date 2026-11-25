package com.milkcheck.service;

import com.milkcheck.dto.PageResponse;
import com.milkcheck.dto.ProductRequest;
import com.milkcheck.dto.ProductResponse;
import com.milkcheck.entity.Category;
import com.milkcheck.entity.Product;
import com.milkcheck.exception.BadRequestException;
import com.milkcheck.exception.DuplicateResourceException;
import com.milkcheck.exception.ResourceNotFoundException;
import com.milkcheck.repository.CategoryRepository;
import com.milkcheck.repository.ProductRepository;
import com.milkcheck.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ---------- Storefront ----------

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Long categoryId,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                boolean inStockOnly, String sort,
                                                int page, int size) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Minimum price cannot exceed maximum price");
        }
        Page<Product> result = productRepository.search(
                blankToNull(keyword), categoryId, minPrice, maxPrice, inStockOnly,
                PageUtil.of(page, size, resolveSort(sort)));
        return PageResponse.from(result.map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> featured() {
        return productRepository.findTop8ByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getActive(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    // ---------- Admin ----------

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchForAdmin(String keyword, int page, int size) {
        Page<Product> result = productRepository.searchForAdmin(
                blankToNull(keyword), PageUtil.of(page, size, Sort.by("id").descending()));
        return PageResponse.from(result.map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductResponse getForAdmin(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        String name = req.name().trim();
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A product named '" + name + "' already exists");
        }
        Product product = new Product();
        apply(product, req);
        product.setActive(req.active() == null || req.active());
        Product saved = productRepository.save(product);
        log.info("Product created: id={} name={}", saved.getId(), saved.getName());
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product product = findOrThrow(id);
        String name = req.name().trim();
        if (!name.equalsIgnoreCase(product.getName()) && productRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A product named '" + name + "' already exists");
        }
        apply(product, req);
        if (req.active() != null) {
            product.setActive(req.active());
        }
        log.info("Product updated: id={}", id);
        return ProductResponse.from(product);
    }

    /** Soft delete: hides the product but keeps order history intact. */
    @Transactional
    public void delete(Long id) {
        Product product = findOrThrow(id);
        product.setActive(false);
        log.info("Product deactivated: id={} name={}", id, product.getName());
    }

    // ---------- Helpers ----------

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private void apply(Product p, ProductRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", req.categoryId()));
        p.setName(req.name().trim());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStockQuantity(req.stockQuantity());
        p.setImageUrl(blankToNull(req.imageUrl()));
        p.setUnit(req.unit());
        p.setFeatured(req.featured());
        p.setCategory(category);
    }

    private Sort resolveSort(String sort) {
        Sort base = switch (sort == null ? "" : sort) {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name"       -> Sort.by("name").ascending();
            default           -> Sort.by("createdAt").descending();
        };
        return base.and(Sort.by("id").descending());   // stable pagination on ties
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}