package com.tissugest.entity;

import com.tissugest.entity.enums.DocType;
import com.tissugest.entity.enums.QuoteStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "merchant"})
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "shop"})
    private Client client;

    @Column(name = "quote_number", nullable = false, length = 50)
    private String quoteNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, columnDefinition = "document_type")
    @Builder.Default
    private DocType docType = DocType.QUOTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "quote_status")
    @Builder.Default
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private Long totalAmount = 0L;

    @Column(name = "discount_amount", nullable = false)
    @Builder.Default
    private Long discountAmount = 0L;

    private String notes;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "converted_sale_id")
    private Long convertedSaleId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"quote"})
    @Builder.Default
    private List<QuoteLine> lines = new ArrayList<>();
}
