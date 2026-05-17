package com.tissugest.entity;

import com.tissugest.entity.enums.SupplyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Supply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private Long totalAmount = 0L;

    @Column(name = "amount_paid", nullable = false)
    @Builder.Default
    private Long amountPaid = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "supply_status")
    @Builder.Default
    private SupplyStatus status = SupplyStatus.UNPAID;

    @Column(name = "supply_date", nullable = false)
    @Builder.Default
    private LocalDate supplyDate = LocalDate.now();

    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "supply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplyLine> lines = new ArrayList<>();
}
