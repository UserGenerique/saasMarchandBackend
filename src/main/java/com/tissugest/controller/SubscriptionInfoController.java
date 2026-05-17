package com.tissugest.controller;

import com.tissugest.entity.Subscription;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.repository.PlanFeatureRepository;
import com.tissugest.service.SubscriptionService;
import com.tissugest.security.SecurityHelper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns the current merchant's subscription info + active features.
 * Used by the mobile app to show/hide features based on plan.
 */
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionInfoController {

    private final SubscriptionService subscriptionService;
    private final PlanFeatureRepository planFeatureRepository;
    private final SecurityHelper securityHelper;

    @GetMapping("/info")
    public ResponseEntity<SubscriptionInfo> getInfo() {
        Long merchantId = subscriptionService.getMerchantIdByUserId(securityHelper.getCurrentUserId());
        Subscription sub = subscriptionService.checkActiveSubscription(merchantId);

        List<String> features = planFeatureRepository.findByPlanId(sub.getPlan().getId())
                .stream()
                .map(pf -> pf.getFeatureCode().name())
                .collect(Collectors.toList());

        return ResponseEntity.ok(SubscriptionInfo.builder()
                .planName(sub.getPlan().getName())
                .status(sub.getStatus().name())
                .endDate(sub.getEndDate())
                .features(features)
                .build());
    }

    @Data
    @Builder
    public static class SubscriptionInfo {
        private String planName;
        private String status;
        private LocalDate endDate;
        private List<String> features;
    }
}
