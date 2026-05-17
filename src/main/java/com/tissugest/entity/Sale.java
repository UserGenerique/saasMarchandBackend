package com.tissugest.entity;

import com.tissugest.entity.enums.SaleStatus;
import com.tissugest.entity.enums.SaleType;
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
@Table(name = "sales")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Sale {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = false, columnDefinition = "sale_type")
    private SaleType saleType;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private Long totalAmount = 0L;

    @Column(name = "discount_amount", nullable = false)
    @Builder.Default
    private Long discountAmount = 0L;

    @Column(name = "amount_paid", nullable = false)
    @Builder.Default
    private Long amountPaid = 0L;

    @Column(name = "remaining_amount", nullable = false)
    @Builder.Default
    private Long remainingAmount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "sale_status")
    @Builder.Default
    private SaleStatus status = SaleStatus.COMPLETED;

    @Column(name = "sale_date", nullable = false)
    @Builder.Default
    private LocalDate saleDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "merchant", "passwordHash"})
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"sale"})
    @Builder.Default
    private List<SaleLine> lines = new ArrayList<>();
}
