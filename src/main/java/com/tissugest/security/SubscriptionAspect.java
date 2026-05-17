package com.tissugest.security;

import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.entity.enums.UserRole;
import com.tissugest.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionAspect {

    private final SubscriptionService subscriptionService;

    /**
     * Intercepte les méthodes annotées @RequiresFeature et vérifie
     * que le commerçant a un abonnement actif avec la feature requise.
     */
    @Before("@annotation(requiresFeature)")
    public void checkFeatureAccess(JoinPoint joinPoint, RequiresFeature requiresFeature) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return; // Spring Security gère l'auth, on ne fait rien ici
        }

        // Les admins ne sont pas soumis aux vérifications d'abonnement
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.ADMIN.name()));
        if (isAdmin) {
            return;
        }

        Long userId = (Long) auth.getPrincipal();
        FeatureCode feature = requiresFeature.value();

        Long merchantId = subscriptionService.getMerchantIdByUserId(userId);
        subscriptionService.checkFeatureAccess(merchantId, feature);

        log.debug("Feature access granted: user={}, feature={}", userId, feature);
    }
}
