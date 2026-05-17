package com.tissugest.repository;

import com.tissugest.entity.Subscription;
import com.tissugest.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByMerchantIdAndStatusOrderByEndDateDesc(Long merchantId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.merchant.id = :merchantId AND s.status = 'ACTIVE' ORDER BY s.endDate DESC")
    Optional<Subscription> findActiveByMerchantId(Long merchantId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate < :date")
    List<Subscription> findExpiredSubscriptions(LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :from AND :to")
    List<Subscription> findExpiringSoon(LocalDate from, LocalDate to);
}
