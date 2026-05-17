package com.tissugest.repository;

import com.tissugest.entity.Supply;
import com.tissugest.entity.enums.SupplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
    List<Supply> findByShopIdOrderByCreatedAtDesc(Long shopId);
    List<Supply> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);
    Optional<Supply> findByIdAndShopId(Long id, Long shopId);
    List<Supply> findByShopIdAndStatus(Long shopId, SupplyStatus status);

    @Query("SELECT COALESCE(SUM(s.totalAmount - s.amountPaid), 0) FROM Supply s WHERE s.supplier.id = :supplierId AND s.status != 'PAID'")
    Long calculateDebtToSupplier(Long supplierId);
}
