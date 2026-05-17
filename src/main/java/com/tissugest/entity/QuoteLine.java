package com.tissugest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_lines")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class QuoteLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "lines"})
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "shop", "category", "stock"})
    private Product product;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price", nullable = false)
    @Builder.Default
    private Long unitPrice = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long discount = 0L;

    @Column(name = "line_total", nullable = false)
    @Builder.Default
    private Long lineTotal = 0L;
}
