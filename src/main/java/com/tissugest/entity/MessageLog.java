package com.tissugest.entity;

import com.tissugest.entity.enums.MessagingProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "recipient_name")
    private String recipientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "messaging_provider")
    private MessagingProvider provider;

    @Column(name = "message_type", nullable = false, length = 50)
    private String messageType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SENT";

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
