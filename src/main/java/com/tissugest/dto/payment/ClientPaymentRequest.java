package com.tissugest.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientPaymentRequest {
    @NotNull(message = "La vente est obligatoire")
    private Long saleId;
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private Long amount;
    private LocalDate paymentDate; // null = aujourd'hui
    private String note;
}
