package com.tissugest.service;

import com.tissugest.dto.admin.AdminStatsResponse;
import com.tissugest.dto.admin.AssignPlanRequest;
import com.tissugest.dto.admin.SubscriptionPaymentRequest;
import com.tissugest.entity.*;
import com.tissugest.entity.enums.SubscriptionStatus;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.*;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    public List<Subscription> listAll() {
        return subscriptionRepository.findAll();
    }

    public List<Subscription> listExpiringSoon() {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(7);
        return subscriptionRepository.findExpiringSoon(from, to);
    }

    @Transactional
    public Subscription assignPlan(AssignPlanRequest request) {
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> BusinessException.notFound("Commerçant", request.getMerchantId()));
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> BusinessException.notFound("Plan", request.getPlanId()));

        // Expirer les abonnements actifs existants
        subscriptionRepository.findActiveByMerchantId(merchant.getId())
                .ifPresent(existing -> {
                    existing.setStatus(SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(existing);
                });

        Subscription subscription = Subscription.builder()
                .merchant(merchant)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(plan.getDurationDays()))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        subscription = subscriptionRepository.save(subscription);

        log.info("Plan '{}' attribué au commerçant {} (merchant={})",
                plan.getName(), merchant.getBusinessName(), merchant.getId());
        return subscription;
    }

    @Transactional
    public SubscriptionPayment recordPayment(Long subscriptionId, SubscriptionPaymentRequest request) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> BusinessException.notFound("Abonnement", subscriptionId));

        Long adminUserId = securityHelper.getCurrentUserId();
        User admin = userRepository.findById(adminUserId).orElse(null);

        SubscriptionPayment payment = SubscriptionPayment.builder()
                .subscription(subscription)
                .amountFcfa(request.getAmountFcfa())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .paymentMethod(request.getPaymentMethod())
                .reference(request.getReference())
                .recordedBy(admin)
                .build();
        payment = paymentRepository.save(payment);

        log.info("Paiement abonnement enregistré: subscription={}, montant={}",
                subscriptionId, request.getAmountFcfa());
        return payment;
    }

    public List<SubscriptionPayment> listPayments(Long subscriptionId) {
        return paymentRepository.findBySubscriptionIdOrderByPaymentDateDesc(subscriptionId);
    }

    public AdminStatsResponse getStats() {
        long totalMerchants = merchantRepository.count();
        long activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE).size();
        long expiredSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.EXPIRED).size();
        long suspendedSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.SUSPENDED).size();

        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = firstOfMonth.plusMonths(1).minusDays(1);
        Long revenueMonth = paymentRepository.totalRevenueBetween(firstOfMonth, endOfMonth);
        Long revenueTotal = paymentRepository.totalRevenueBetween(LocalDate.of(2000, 1, 1), LocalDate.now());

        long expiringNext7 = subscriptionRepository.findExpiringSoon(LocalDate.now(), LocalDate.now().plusDays(7)).size();

        return AdminStatsResponse.builder()
                .totalMerchants(totalMerchants)
                .activeMerchants(activeSubscriptions)
                .expiredMerchants(expiredSubscriptions)
                .suspendedMerchants(suspendedSubscriptions)
                .totalSubscriptions(subscriptionRepository.count())
                .activeSubscriptions(activeSubscriptions)
                .revenueCurrentMonth(revenueMonth)
                .revenueTotal(revenueTotal)
                .expiringNext7Days(expiringNext7)
                .build();
    }
}
