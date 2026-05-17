package com.tissugest.dto.supply;

import com.tissugest.entity.Supply;
import com.tissugest.entity.SupplyLine;
import com.tissugest.entity.enums.SupplyStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class SupplyResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private Long totalAmount;
    private Long amountPaid;
    private Long remainingAmount;
    private SupplyStatus status;
    private LocalDate supplyDate;
    private String notes;
    private LocalDateTime createdAt;
    private List<SupplyLineResponse> lines;

    @Data
    @Builder
    public static class SupplyLineResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal quantity;
        private Long unitPrice;
        private Long lineTotal;
    }

    public static SupplyResponse from(Supply s) {
        return SupplyResponse.builder()
                .id(s.getId())
                .supplierId(s.getSupplier().getId())
                .supplierName(s.getSupplier().getName())
                .totalAmount(s.getTotalAmount())
                .amountPaid(s.getAmountPaid())
                .remainingAmount(s.getTotalAmount() - s.getAmountPaid())
                .status(s.getStatus())
                .supplyDate(s.getSupplyDate())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .lines(s.getLines().stream().map(SupplyResponse::mapLine).collect(Collectors.toList()))
                .build();
    }

    private static SupplyLineResponse mapLine(SupplyLine l) {
        return SupplyLineResponse.builder()
                .id(l.getId())
                .productId(l.getProduct().getId())
                .productName(l.getProduct().getName())
                .quantity(l.getQuantity())
                .unitPrice(l.getUnitPrice())
                .lineTotal(l.getLineTotal())
                .build();
    }
}
