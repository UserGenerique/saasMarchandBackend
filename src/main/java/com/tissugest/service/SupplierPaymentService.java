package com.tissugest.service;

import com.tissugest.dto.payment.SupplierPaymentRequest;
import com.tissugest.entity.*;
import com.tissugest.entity.enums.SupplyStatus;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.SupplierPaymentRepository;
import com.tissugest.repository.SupplyRepository;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierPaymentService {

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplyRepository supplyRepository;
    private final SecurityHelper securityHelper;

    public List<SupplierPayment> listBySupply(Long supplyId) {
        return supplierPaymentRepository.findBySupplyIdOrderByCreatedAtDesc(supplyId);
    }

    public List<SupplierPayment> listBySupplier(Long supplierId) {
        return supplierPaymentRepository.findBySupplierIdOrderByCreatedAtDesc(supplierId);
    }

    @Transactional
    public SupplierPayment create(SupplierPaymentRequest request) {
        Shop shop = securityHelper.getCurrentShop();

        Supply supply = supplyRepository.findByIdAndShopId(request.getSupplyId(), shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Approvisionnement", request.getSupplyId()));

        if (supply.getStatus() == SupplyStatus.PAID) {
            throw BusinessException.badRequest("Cet approvisionnement est déjà entièrement payé");
        }

        long remaining = supply.getTotalAmount() - supply.getAmountPaid();
        if (request.getAmount() > remaining) {
            throw BusinessException.badRequest("Le montant dépasse le solde restant ("
                    + remaining + " FCFA)");
        }

        SupplierPayment payment = SupplierPayment.builder()
                .supply(supply)
                .supplier(supply.getSupplier())
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .note(request.getNote())
                .build();
        payment = supplierPaymentRepository.save(payment);

        // Mettre à jour l'approvisionnement
        supply.setAmountPaid(supply.getAmountPaid() + request.getAmount());
        if (supply.getAmountPaid() >= supply.getTotalAmount()) {
            supply.setAmountPaid(supply.getTotalAmount());
            supply.setStatus(SupplyStatus.PAID);
            log.info("Approvisionnement {} entièrement payé", supply.getId());
        } else {
            supply.setStatus(SupplyStatus.PARTIAL);
        }
        supplyRepository.save(supply);

        log.info("Paiement fournisseur enregistré: supply={}, montant={}, restant={}",
                supply.getId(), request.getAmount(), supply.getTotalAmount() - supply.getAmountPaid());
        return payment;
    }
}
