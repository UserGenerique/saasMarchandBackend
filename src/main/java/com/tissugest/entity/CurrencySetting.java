package com.tissugest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_settings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CurrencySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = false;

    @Column(name = "local_currency_name", length = 50)
    @Builder.Default
    private String localCurrencyName = "";

    @Column(name = "conversion_rate", nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal conversionRate = BigDecimal.ONE;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
