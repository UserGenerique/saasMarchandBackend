package com.tissugest.repository;

import com.tissugest.entity.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
    List<SubscriptionPayment> findBySubscriptionIdOrderByPaymentDateDesc(Long subscriptionId);

    @Query("SELECT COALESCE(SUM(sp.amountFcfa), 0) FROM SubscriptionPayment sp " +
           "WHERE sp.subscription.merchant.id = :merchantId")
    Long totalPaidByMerchant(Long merchantId);

    @Query("SELECT COALESCE(SUM(sp.amountFcfa), 0) FROM SubscriptionPayment sp " +
           "WHERE sp.paymentDate BETWEEN :from AND :to")
    Long totalRevenueBetween(LocalDate from, LocalDate to);
}
