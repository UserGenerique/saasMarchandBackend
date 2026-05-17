package com.tissugest.dto.stock;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockAdjustRequest {
    @NotNull(message = "La nouvelle quantité est obligatoire")
    private BigDecimal newQuantity;
    private String note;
}
