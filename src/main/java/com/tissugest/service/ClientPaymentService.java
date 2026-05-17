package com.tissugest.service;

import com.tissugest.dto.payment.ClientPaymentRequest;
import com.tissugest.entity.*;
import com.tissugest.entity.enums.SaleStatus;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.ClientPaymentRepository;
import com.tissugest.repository.SaleRepository;
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
public class ClientPaymentService {

    private final ClientPaymentRepository clientPaymentRepository;
    private final SaleRepository saleRepository;
    private final SecurityHelper securityHelper;

    public List<ClientPayment> listBySale(Long saleId) {
        return clientPaymentRepository.findBySaleIdOrderByCreatedAtDesc(saleId);
    }

    public List<ClientPayment> listByClient(Long clientId) {
        return clientPaymentRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Transactional
    public ClientPayment create(ClientPaymentRequest request) {
        Shop shop = securityHelper.getCurrentShop();

        Sale sale = saleRepository.findByIdAndShopId(request.getSaleId(), shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Vente", request.getSaleId()));

        if (sale.getStatus() != SaleStatus.CREDIT_OPEN) {
            throw BusinessException.badRequest("Cette vente n'a pas de crédit ouvert");
        }

        if (sale.getClient() == null) {
            throw BusinessException.badRequest("Cette vente n'a pas de client associé");
        }

        if (request.getAmount() > sale.getRemainingAmount()) {
            throw BusinessException.badRequest("Le montant dépasse le solde restant ("
                    + sale.getRemainingAmount() + " FCFA)");
        }

        ClientPayment payment = ClientPayment.builder()
                .sale(sale)
                .client(sale.getClient())
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .note(request.getNote())
                .build();
        payment = clientPaymentRepository.save(payment);

        // Mettre à jour la vente
        sale.setAmountPaid(sale.getAmountPaid() + request.getAmount());
        sale.setRemainingAmount(sale.getTotalAmount() - sale.getAmountPaid());

        if (sale.getRemainingAmount() <= 0) {
            sale.setRemainingAmount(0L);
            sale.setStatus(SaleStatus.CREDIT_SETTLED);
            log.info("Vente {} entièrement soldée", sale.getId());
        }
        saleRepository.save(sale);

        log.info("Paiement client enregistré: vente={}, montant={}, restant={}",
                sale.getId(), request.getAmount(), sale.getRemainingAmount());
        return payment;
    }
}
