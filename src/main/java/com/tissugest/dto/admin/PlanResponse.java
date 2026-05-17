package com.tissugest.dto.admin;

import com.tissugest.entity.SubscriptionPlan;
import com.tissugest.entity.enums.FeatureCode;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PlanResponse {
    private Long id;
    private String name;
    private String description;
    private Long priceFcfa;
    private Integer durationDays;
    private Boolean isTrial;
    private Boolean isActive;
    private List<FeatureCode> features;

    public static PlanResponse from(SubscriptionPlan p) {
        return PlanResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .priceFcfa(p.getPriceFcfa())
                .durationDays(p.getDurationDays())
                .isTrial(p.getIsTrial())
                .isActive(p.getIsActive())
                .features(p.getFeatures().stream()
                        .map(f -> f.getFeatureCode())
                        .collect(Collectors.toList()))
                .build();
    }
}
