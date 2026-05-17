package com.tissugest.dto.sale;

import com.tissugest.entity.enums.SaleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaleRequest {
    @NotNull(message = "Le type de vente est obligatoire")
    private SaleType saleType;

    private Long clientId; // requis pour WITH_CLIENT et CREDIT
    private Long amountPaid = 0L; // montant versé à la vente (acompte pour crédit)

    @NotEmpty(message = "Au moins une ligne de vente est requise")
    @Valid
    private List<SaleLineRequest> lines;

    private LocalDate saleDate; // null = aujourd'hui

    @Data
    public static class SaleLineRequest {
        @NotNull(message = "Le produit est obligatoire")
        private Long productId;
        @NotNull(message = "La quantité est obligatoire")
        private BigDecimal quantity;
        private Long unitPrice; // null = prix de vente du produit
        private Long discount = 0L; // remise en FCFA sur la ligne
    }
}
