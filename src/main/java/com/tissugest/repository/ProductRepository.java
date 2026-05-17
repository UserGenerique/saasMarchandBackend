package com.tissugest.repository;

import com.tissugest.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopIdAndIsActiveTrue(Long shopId);
    List<Product> findByShopIdAndCategoryIdAndIsActiveTrue(Long shopId, Long categoryId);
    Optional<Product> findByIdAndShopId(Long id, Long shopId);

    @Query("SELECT p FROM Product p JOIN p.stock s WHERE p.shop.id = :shopId AND p.isActive = true AND s.quantity <= p.lowStockThreshold")
    List<Product> findLowStockProducts(Long shopId);
}
