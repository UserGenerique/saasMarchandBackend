package com.tissugest.service.messaging;

/**
 * Abstraction for sending messages (SMS or WhatsApp).
 * Implementations are selected based on the shop's messaging_provider config.
 */
public interface MessagingService {

    /**
     * Send a message using shop-specific credentials.
     * @return external ID from the provider (for tracking), or null
     */
    String sendMessage(String phone, String message, ShopCredentials credentials) throws MessagingException;

    String getProviderName();

    /**
     * Shop-level API credentials.
     */
    record ShopCredentials(String apiKey, String apiSecret, String sender, String phoneId) {};

    class MessagingException extends Exception {
        public MessagingException(String message) { super(message); }
        public MessagingException(String message, Throwable cause) { super(message, cause); }
    }
}
