package com.tissugest.repository;

import com.tissugest.entity.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {
    List<SupplierPayment> findBySupplyIdOrderByCreatedAtDesc(Long supplyId);
    List<SupplierPayment> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);
}
