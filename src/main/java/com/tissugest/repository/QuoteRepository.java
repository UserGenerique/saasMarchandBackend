package com.tissugest.repository;

import com.tissugest.entity.Quote;
import com.tissugest.entity.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByShopIdOrderByCreatedAtDesc(Long shopId);
    Optional<Quote> findByIdAndShopId(Long id, Long shopId);
    List<Quote> findByShopIdAndStatus(Long shopId, QuoteStatus status);
}
