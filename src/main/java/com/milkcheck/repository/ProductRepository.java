package com.milkcheck.repository;

import com.milkcheck.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndActiveTrue(Long id);

    List<Product> findTop8ByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();

    long countByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    /** Storefront search: all filters are optional (pass null to skip). */
    @Query("""
           SELECT p FROM Product p
           WHERE p.active = true
             AND (:categoryId IS NULL OR p.category.id = :categoryId)
             AND (:keyword IS NULL
                  OR LOWER(p.name)        LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:minPrice IS NULL OR p.price >= :minPrice)
             AND (:maxPrice IS NULL OR p.price <= :maxPrice)
             AND (:inStockOnly = false OR p.stockQuantity > 0)
           """)
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("inStockOnly") boolean inStockOnly,
                         Pageable pageable);

    /** Admin listing: includes inactive products. */
    @Query("""
           SELECT p FROM Product p
           WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Product> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);


    

    /** Atomic stock decrement. Returns 0 rows updated if stock is insufficient. */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty " +
           "WHERE p.id = :id AND p.stockQuantity >= :qty")
    int decrementStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :qty WHERE p.id = :id")
    int incrementStock(@Param("id") Long id, @Param("qty") int qty);
}