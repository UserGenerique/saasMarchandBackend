package com.tissugest.entity;

import com.tissugest.entity.enums.MessagingProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shops")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "messaging_provider", nullable = false, columnDefinition = "messaging_provider")
    @Builder.Default
    private MessagingProvider messagingProvider = MessagingProvider.NONE;

    @Column(name = "messaging_enabled", nullable = false)
    @Builder.Default
    private Boolean messagingEnabled = false;

    @Column(name = "messaging_api_key", length = 500)
    private String messagingApiKey;

    @Column(name = "messaging_api_secret", length = 500)
    private String messagingApiSecret;

    @Column(name = "messaging_sender", length = 100)
    @Builder.Default
    private String messagingSender = "TissuGest";

    @Column(name = "messaging_phone_id", length = 100)
    private String messagingPhoneId;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "receipt_footer")
    private String receiptFooter;

    @Column(name = "country_code", length = 5)
    @Builder.Default
    private String countryCode = "CI";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
