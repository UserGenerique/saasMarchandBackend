package com.tissugest.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalMerchants;
    private long activeMerchants;
    private long expiredMerchants;
    private long suspendedMerchants;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private Long revenueCurrentMonth;
    private Long revenueTotal;
    private long expiringNext7Days;
}
