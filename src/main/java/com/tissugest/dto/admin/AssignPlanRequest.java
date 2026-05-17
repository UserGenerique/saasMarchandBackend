package com.tissugest.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignPlanRequest {
    @NotNull(message = "Le commerçant est obligatoire")
    private Long merchantId;
    @NotNull(message = "Le plan est obligatoire")
    private Long planId;
}
