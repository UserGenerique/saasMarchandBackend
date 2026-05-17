package com.tissugest.dto.sale;

import com.tissugest.entity.Sale;
import com.tissugest.entity.SaleLine;
import com.tissugest.entity.enums.SaleStatus;
import com.tissugest.entity.enums.SaleType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class SaleResponse {
    private Long id;
    private SaleType saleType;
    private Long clientId;
    private String clientName;
    private Long totalAmount;
    private Long discountAmount;
    private Long amountPaid;
    private Long remainingAmount;
    private SaleStatus status;
    private LocalDate saleDate;
    private LocalDateTime createdAt;
    private List<SaleLineResponse> lines;

    @Data
    @Builder
    public static class SaleLineResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal quantity;
        private Long unitPrice;
        private Long discount;
        private Long lineTotal;
    }

    public static SaleResponse from(Sale s) {
        return SaleResponse.builder()
                .id(s.getId())
                .saleType(s.getSaleType())
                .clientId(s.getClient() != null ? s.getClient().getId() : null)
                .clientName(s.getClient() != null ? s.getClient().getName() : null)
                .totalAmount(s.getTotalAmount())
                .discountAmount(s.getDiscountAmount())
                .amountPaid(s.getAmountPaid())
                .remainingAmount(s.getRemainingAmount())
                .status(s.getStatus())
                .saleDate(s.getSaleDate())
                .createdAt(s.getCreatedAt())
                .lines(s.getLines().stream().map(SaleResponse::mapLine).collect(Collectors.toList()))
                .build();
    }

    private static SaleLineResponse mapLine(SaleLine l) {
        return SaleLineResponse.builder()
                .id(l.getId())
                .productId(l.getProduct().getId())
                .productName(l.getProduct().getName())
                .quantity(l.getQuantity())
                .unitPrice(l.getUnitPrice())
                .discount(l.getDiscount())
                .lineTotal(l.getLineTotal())
                .build();
    }
}
