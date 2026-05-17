package com.tissugest.dto.admin;

import com.tissugest.entity.enums.FeatureCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PlanRequest {
    @NotBlank(message = "Le nom du plan est obligatoire")
    private String name;
    private String description;
    @NotNull(message = "Le prix est obligatoire")
    private Long priceFcfa;
    private Integer durationDays = 30;
    private Boolean isTrial = false;
    private List<FeatureCode> features;
}
