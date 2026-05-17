package com.tissugest.repository;

import com.tissugest.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByMerchantId(Long merchantId);
    List<Shop> findByMerchantIdAndIsActiveTrue(Long merchantId);
}
