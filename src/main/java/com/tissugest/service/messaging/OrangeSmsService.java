package com.tissugest.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Orange SMS API integration for Côte d'Ivoire, Mali, Sénégal.
 * In dev mode (simulate=true), messages are just logged.
 * In prod, call Orange SMS API: https://developer.orange.com/apis/sms-ci/overview
 *
 * Required env vars in prod:
 *   ORANGE_SMS_CLIENT_ID, ORANGE_SMS_CLIENT_SECRET, ORANGE_SMS_SENDER
 */
@Service("orangeSmsService")
@Slf4j
public class OrangeSmsService implements MessagingService {

    @Value("${app.messaging.orange.simulate:true}")
    private boolean simulate;

    @Value("${app.messaging.orange.client-id:}")
    private String clientId;

    @Value("${app.messaging.orange.client-secret:}")
    private String clientSecret;

    @Value("${app.messaging.orange.sender:TissuGest}")
    private String senderName;

    @Override
    public String sendMessage(String phone, String message, ShopCredentials credentials) throws MessagingException {
        if (simulate) {
            log.info("[ORANGE SMS SIMULATED] To: {}, From: {}, Message: {}", phone, credentials.sender(), message);
            return "SIM-" + System.currentTimeMillis();
        }

        String key = credentials.apiKey() != null ? credentials.apiKey() : clientId;
        String secret = credentials.apiSecret() != null ? credentials.apiSecret() : clientSecret;
        String sender = credentials.sender() != null ? credentials.sender() : senderName;

        if (key.isEmpty() || secret.isEmpty()) {
            throw new MessagingException("Credentials Orange SMS non configurées pour cette boutique.");
        }

        // TODO: Real Orange SMS API call
        // 1. POST https://api.orange.com/oauth/v3/token (client_credentials with key/secret)
        // 2. POST https://api.orange.com/smsmessaging/v1/outbound/tel:{sender}/requests
        try {
            log.warn("[ORANGE SMS] Real API call placeholder. Key={}, Sender={}", key, sender);
            throw new MessagingException("Implémentation Orange SMS API en cours.");
        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException("Erreur envoi SMS Orange: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "ORANGE_SMS";
    }
}
