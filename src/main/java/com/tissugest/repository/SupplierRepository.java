package com.tissugest.repository;

import com.tissugest.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByShopId(Long shopId);
    Optional<Supplier> findByIdAndShopId(Long id, Long shopId);
}
