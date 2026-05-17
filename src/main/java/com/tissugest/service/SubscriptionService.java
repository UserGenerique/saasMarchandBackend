package com.tissugest.service;

import com.tissugest.entity.Subscription;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.MerchantRepository;
import com.tissugest.repository.PlanFeatureRepository;
import com.tissugest.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MerchantRepository merchantRepository;
    private final PlanFeatureRepository planFeatureRepository;

    /**
     * Vérifie que le commerçant a un abonnement actif.
     * Lève une exception si l'abonnement est expiré ou suspendu.
     */
    public Subscription checkActiveSubscription(Long merchantId) {
        Subscription subscription = subscriptionRepository.findActiveByMerchantId(merchantId)
                .orElseThrow(BusinessException::subscriptionExpired);

        if (!subscription.isEffectivelyActive()) {
            throw BusinessException.subscriptionExpired();
        }

        return subscription;
    }

    /**
     * Vérifie que le commerçant a accès à une fonctionnalité spécifique.
     */
    public void checkFeatureAccess(Long merchantId, FeatureCode featureCode) {
        Subscription subscription = checkActiveSubscription(merchantId);

        boolean hasFeature = planFeatureRepository.existsByPlanIdAndFeatureCode(
                subscription.getPlan().getId(), featureCode);

        if (!hasFeature) {
            throw BusinessException.featureNotAvailable(featureCode.name());
        }
    }

    /**
     * Retourne le merchantId à partir du userId (pour les commerçants).
     */
    public Long getMerchantIdByUserId(Long userId) {
        return merchantRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("Commerçant", userId))
                .getId();
    }
}
