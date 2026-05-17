package com.tissugest.dto.supply;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SupplyRequest {
    @NotNull(message = "Le fournisseur est obligatoire")
    private Long supplierId;

    private Long amountPaid = 0L;
    private String notes;
    private LocalDate supplyDate; // null = aujourd'hui

    // Échéances optionnelles pour paiement échelonné
    private List<ScheduleEntry> schedules;

    @NotEmpty(message = "Au moins une ligne d'approvisionnement est requise")
    @Valid
    private List<SupplyLineRequest> lines;

    @Data
    public static class SupplyLineRequest {
        @NotNull(message = "Le produit est obligatoire")
        private Long productId;
        @NotNull(message = "La quantité est obligatoire")
        private BigDecimal quantity;
        @NotNull(message = "Le prix unitaire est obligatoire")
        private Long unitPrice;
    }

    @Data
    public static class ScheduleEntry {
        @NotNull(message = "Le montant dû est obligatoire")
        private Long amountDue;
        @NotNull(message = "La date d'échéance est obligatoire")
        private LocalDate dueDate;
    }
}
