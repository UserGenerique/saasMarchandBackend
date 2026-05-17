package com.tissugest.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * WhatsApp Cloud API integration (Meta Business API).
 * In dev mode (simulate=true), messages are just logged.
 * In prod, call WhatsApp Cloud API: https://developers.facebook.com/docs/whatsapp/cloud-api
 *
 * Required env vars in prod:
 *   WHATSAPP_TOKEN, WHATSAPP_PHONE_NUMBER_ID
 */
@Service("whatsAppService")
@Slf4j
public class WhatsAppService implements MessagingService {

    @Value("${app.messaging.whatsapp.simulate:true}")
    private boolean simulate;

    @Value("${app.messaging.whatsapp.token:}")
    private String token;

    @Value("${app.messaging.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Override
    public String sendMessage(String phone, String message, ShopCredentials credentials) throws MessagingException {
        if (simulate) {
            log.info("[WHATSAPP SIMULATED] To: {}, Message: {}", phone, message);
            return "WA-SIM-" + System.currentTimeMillis();
        }

        String apiToken = credentials.apiKey() != null ? credentials.apiKey() : token;
        String numberId = credentials.phoneId() != null ? credentials.phoneId() : phoneNumberId;

        if (apiToken.isEmpty() || numberId.isEmpty()) {
            throw new MessagingException("Credentials WhatsApp non configurées pour cette boutique.");
        }

        // TODO: Real WhatsApp Cloud API call
        // POST https://graph.facebook.com/v18.0/{numberId}/messages
        // Headers: Authorization: Bearer {apiToken}
        try {
            log.warn("[WHATSAPP] Real API call placeholder. PhoneNumberId={}", numberId);
            throw new MessagingException("Implémentation WhatsApp API en cours.");
        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException("Erreur envoi WhatsApp: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "WHATSAPP";
    }
}
