package com.tissugest.service;

import com.tissugest.dto.admin.AdminMerchantResponse;
import com.tissugest.entity.Merchant;
import com.tissugest.entity.Subscription;
import com.tissugest.entity.enums.SubscriptionStatus;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.MerchantRepository;
import com.tissugest.repository.SubscriptionRepository;
import com.tissugest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMerchantService {

    private final MerchantRepository merchantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public List<AdminMerchantResponse> listAll() {
        return merchantRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AdminMerchantResponse getById(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> BusinessException.notFound("Commerçant", merchantId));
        return toResponse(merchant);
    }

    @Transactional
    public AdminMerchantResponse suspend(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> BusinessException.notFound("Commerçant", merchantId));

        // Suspendre l'abonnement actif
        subscriptionRepository.findActiveByMerchantId(merchantId)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.SUSPENDED);
                    subscriptionRepository.save(sub);
                });

        // Désactiver l'utilisateur
        merchant.getUser().setIsActive(false);
        userRepository.save(merchant.getUser());

        log.info("Commerçant suspendu: {} ({})", merchant.getBusinessName(), merchantId);
        return toResponse(merchant);
    }

    @Transactional
    public AdminMerchantResponse reactivate(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> BusinessException.notFound("Commerçant", merchantId));

        // Réactiver l'utilisateur
        merchant.getUser().setIsActive(true);
        userRepository.save(merchant.getUser());

        // Réactiver l'abonnement suspendu s'il existe
        subscriptionRepository.findFirstByMerchantIdAndStatusOrderByEndDateDesc(
                merchantId, SubscriptionStatus.SUSPENDED)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.ACTIVE);
                    subscriptionRepository.save(sub);
                });

        log.info("Commerçant réactivé: {} ({})", merchant.getBusinessName(), merchantId);
        return toResponse(merchant);
    }

    private AdminMerchantResponse toResponse(Merchant merchant) {
        Subscription sub = subscriptionRepository.findActiveByMerchantId(merchant.getId())
                .or(() -> subscriptionRepository.findFirstByMerchantIdAndStatusOrderByEndDateDesc(
                        merchant.getId(), SubscriptionStatus.SUSPENDED))
                .or(() -> subscriptionRepository.findFirstByMerchantIdAndStatusOrderByEndDateDesc(
                        merchant.getId(), SubscriptionStatus.EXPIRED))
                .orElse(null);
        return AdminMerchantResponse.from(merchant, sub);
    }
}
