package com.tissugest.controller;

import com.tissugest.entity.MessageLog;
import com.tissugest.entity.Shop;
import com.tissugest.entity.enums.MessagingProvider;
import com.tissugest.repository.MessageLogRepository;
import com.tissugest.repository.ShopRepository;
import com.tissugest.exception.BusinessException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/messaging")
@RequiredArgsConstructor
public class AdminMessagingController {

    private final ShopRepository shopRepository;
    private final MessageLogRepository messageLogRepository;

    /**
     * Get messaging config for a merchant's shop.
     */
    @GetMapping("/config/{merchantId}")
    public ResponseEntity<MessagingConfig> getConfig(@PathVariable Long merchantId) {
        Shop shop = shopRepository.findByMerchantIdAndIsActiveTrue(merchantId)
                .stream().findFirst()
                .orElseThrow(() -> BusinessException.notFound("Boutique", merchantId));

        return ResponseEntity.ok(MessagingConfig.from(shop));
    }

    /**
     * Update messaging config for a merchant's shop.
     */
    @PutMapping("/config/{merchantId}")
    public ResponseEntity<MessagingConfig> updateConfig(
            @PathVariable Long merchantId,
            @RequestBody MessagingConfigRequest request) {

        Shop shop = shopRepository.findByMerchantIdAndIsActiveTrue(merchantId)
                .stream().findFirst()
                .orElseThrow(() -> BusinessException.notFound("Boutique", merchantId));

        shop.setMessagingProvider(MessagingProvider.valueOf(request.provider));
        shop.setMessagingEnabled(request.enabled);
        shop.setMessagingApiKey(request.apiKey);
        shop.setMessagingApiSecret(request.apiSecret);
        shop.setMessagingSender(request.sender);
        shop.setMessagingPhoneId(request.phoneId);
        shop.setCountryCode(request.countryCode);

        shop = shopRepository.save(shop);
        return ResponseEntity.ok(MessagingConfig.from(shop));
    }

    /**
     * Get message logs for a merchant's shop.
     */
    @GetMapping("/logs/{merchantId}")
    public ResponseEntity<List<MessageLog>> getLogs(@PathVariable Long merchantId) {
        Shop shop = shopRepository.findByMerchantIdAndIsActiveTrue(merchantId)
                .stream().findFirst()
                .orElseThrow(() -> BusinessException.notFound("Boutique", merchantId));

        return ResponseEntity.ok(messageLogRepository.findByShopIdOrderByCreatedAtDesc(shop.getId()));
    }

    // DTOs
    @Data
    public static class MessagingConfigRequest {
        String provider = "NONE";
        boolean enabled = false;
        String apiKey;
        String apiSecret;
        String sender = "TissuGest";
        String phoneId;
        String countryCode = "CI";
    }

    @Data
    public static class MessagingConfig {
        Long shopId;
        String shopName;
        String provider;
        boolean enabled;
        boolean hasCredentials;
        String sender;
        String countryCode;
        long messagesSent;

        static MessagingConfig from(Shop shop) {
            MessagingConfig config = new MessagingConfig();
            config.shopId = shop.getId();
            config.shopName = shop.getName();
            config.provider = shop.getMessagingProvider().name();
            config.enabled = shop.getMessagingEnabled();
            config.hasCredentials = shop.getMessagingApiKey() != null && !shop.getMessagingApiKey().isEmpty();
            config.sender = shop.getMessagingSender();
            config.countryCode = shop.getCountryCode();
            return config;
        }
    }
}
