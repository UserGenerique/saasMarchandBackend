package com.tissugest.service.messaging;

import com.tissugest.entity.MessageLog;
import com.tissugest.entity.Shop;
import com.tissugest.entity.enums.MessagingProvider;
import com.tissugest.repository.MessageLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Central dispatcher that sends messages via the shop's configured provider
 * and logs every attempt.
 */
@Service
@Slf4j
public class MessagingDispatcher {

    private final MessagingService orangeSmsService;
    private final MessagingService whatsAppService;
    private final MessageLogRepository messageLogRepository;

    public MessagingDispatcher(
            @Qualifier("orangeSmsService") MessagingService orangeSmsService,
            @Qualifier("whatsAppService") MessagingService whatsAppService,
            MessageLogRepository messageLogRepository) {
        this.orangeSmsService = orangeSmsService;
        this.whatsAppService = whatsAppService;
        this.messageLogRepository = messageLogRepository;
    }

    /**
     * Send a message using the shop's configured provider.
     * Logs the result (success or failure).
     */
    public void send(Shop shop, String phone, String recipientName, String messageType, String content) {
        if (!shop.getMessagingEnabled() || shop.getMessagingProvider() == MessagingProvider.NONE) {
            log.debug("Messaging disabled for shop {}", shop.getId());
            return;
        }

        MessagingService service = switch (shop.getMessagingProvider()) {
            case ORANGE_SMS -> orangeSmsService;
            case WHATSAPP -> whatsAppService;
            default -> null;
        };

        if (service == null) return;

        MessageLog logEntry = MessageLog.builder()
                .shop(shop)
                .recipientPhone(phone)
                .recipientName(recipientName)
                .provider(shop.getMessagingProvider())
                .messageType(messageType)
                .content(content)
                .build();

        MessagingService.ShopCredentials creds = new MessagingService.ShopCredentials(
                shop.getMessagingApiKey(), shop.getMessagingApiSecret(),
                shop.getMessagingSender(), shop.getMessagingPhoneId());

        try {
            String externalId = service.sendMessage(phone, content, creds);
            logEntry.setStatus("SENT");
            logEntry.setExternalId(externalId);
            log.info("Message sent via {} to {} ({})", service.getProviderName(), phone, messageType);
        } catch (MessagingService.MessagingException e) {
            logEntry.setStatus("FAILED");
            logEntry.setErrorMessage(e.getMessage());
            log.warn("Message failed via {} to {}: {}", service.getProviderName(), phone, e.getMessage());
        }

        messageLogRepository.save(logEntry);
    }
}
