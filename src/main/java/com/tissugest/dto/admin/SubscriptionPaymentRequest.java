package com.tissugest.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionPaymentRequest {
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private Long amountFcfa;
    private LocalDate paymentDate; // null = aujourd'hui
    private String paymentMethod;
    private String reference;
}
