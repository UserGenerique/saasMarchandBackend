package com.tissugest.dto.admin;

import com.tissugest.entity.Merchant;
import com.tissugest.entity.Subscription;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminMerchantResponse {
    private Long merchantId;
    private Long userId;
    private String businessName;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private boolean userActive;
    private LocalDateTime createdAt;

    // Subscription info
    private Long subscriptionId;
    private String planName;
    private String subscriptionStatus;
    private LocalDate subscriptionStart;
    private LocalDate subscriptionEnd;

    public static AdminMerchantResponse from(Merchant m, Subscription sub) {
        var builder = AdminMerchantResponse.builder()
                .merchantId(m.getId())
                .userId(m.getUser().getId())
                .businessName(m.getBusinessName())
                .fullName(m.getUser().getFullName())
                .phone(m.getUser().getPhone())
                .email(m.getUser().getEmail())
                .address(m.getAddress())
                .userActive(m.getUser().getIsActive())
                .createdAt(m.getCreatedAt());

        if (sub != null) {
            builder.subscriptionId(sub.getId())
                    .planName(sub.getPlan().getName())
                    .subscriptionStatus(sub.getStatus().name())
                    .subscriptionStart(sub.getStartDate())
                    .subscriptionEnd(sub.getEndDate());
        }
        return builder.build();
    }
}
