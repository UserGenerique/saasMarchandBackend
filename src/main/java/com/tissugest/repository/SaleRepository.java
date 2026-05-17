package com.tissugest.repository;

import com.tissugest.entity.Sale;
import com.tissugest.entity.enums.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByShopIdOrderByCreatedAtDesc(Long shopId);
    Optional<Sale> findByIdAndShopId(Long id, Long shopId);
    List<Sale> findByShopIdAndSaleDateBetweenOrderByCreatedAtDesc(Long shopId, LocalDate from, LocalDate to);
    List<Sale> findByShopIdAndStatus(Long shopId, SaleStatus status);
    List<Sale> findByClientIdOrderByCreatedAtDesc(Long clientId);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.shop.id = :shopId AND s.saleDate = :date AND s.status != 'CANCELLED'")
    Long sumSalesByDate(Long shopId, LocalDate date);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.shop.id = :shopId AND s.saleDate = :date AND s.status != 'CANCELLED'")
    Long countSalesByDate(Long shopId, LocalDate date);

    @Query("SELECT COALESCE(SUM(s.remainingAmount), 0) FROM Sale s WHERE s.client.id = :clientId AND s.status = 'CREDIT_OPEN'")
    Long calculateClientDebt(Long clientId);
}
