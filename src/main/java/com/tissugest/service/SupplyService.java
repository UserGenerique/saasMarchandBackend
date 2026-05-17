package com.tissugest.service;

import com.tissugest.dto.supply.SupplyRequest;
import com.tissugest.dto.supply.SupplyResponse;
import com.tissugest.entity.*;
import com.tissugest.entity.enums.*;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.*;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final StockService stockService;
    private final SecurityHelper securityHelper;

    public List<SupplyResponse> listByShop() {
        Shop shop = securityHelper.getCurrentShop();
        return supplyRepository.findByShopIdOrderByCreatedAtDesc(shop.getId()).stream()
                .map(SupplyResponse::from)
                .collect(Collectors.toList());
    }

    public SupplyResponse getById(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Supply supply = supplyRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Approvisionnement", id));
        return SupplyResponse.from(supply);
    }

    public List<SupplyResponse> listBySupplier(Long supplierId) {
        return supplyRepository.findBySupplierIdOrderByCreatedAtDesc(supplierId).stream()
                .map(SupplyResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplyResponse create(SupplyRequest request) {
        Shop shop = securityHelper.getCurrentShop();

        Supplier supplier = supplierRepository.findByIdAndShopId(request.getSupplierId(), shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Fournisseur", request.getSupplierId()));

        // Construire les lignes
        List<SupplyLine> supplyLines = new ArrayList<>();
        long totalAmount = 0;

        for (SupplyRequest.SupplyLineRequest lineReq : request.getLines()) {
            Product product = productRepository.findByIdAndShopId(lineReq.getProductId(), shop.getId())
                    .orElseThrow(() -> BusinessException.notFound("Produit", lineReq.getProductId()));

            long lineTotal = lineReq.getUnitPrice() * lineReq.getQuantity().longValue();
            SupplyLine line = SupplyLine.builder()
                    .product(product)
                    .quantity(lineReq.getQuantity())
                    .unitPrice(lineReq.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();
            supplyLines.add(line);
            totalAmount += lineTotal;
        }

        // Déterminer le statut de paiement
        long amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : 0L;
        SupplyStatus status;
        if (amountPaid >= totalAmount) {
            status = SupplyStatus.PAID;
            amountPaid = totalAmount;
        } else if (amountPaid > 0) {
            status = SupplyStatus.PARTIAL;
        } else {
            status = SupplyStatus.UNPAID;
        }

        Supply supply = Supply.builder()
                .shop(shop)
                .supplier(supplier)
                .totalAmount(totalAmount)
                .amountPaid(amountPaid)
                .status(status)
                .supplyDate(request.getSupplyDate() != null ? request.getSupplyDate() : java.time.LocalDate.now())
                .notes(request.getNotes())
                .build();
        supply = supplyRepository.save(supply);

        // Sauvegarder les lignes
        for (SupplyLine line : supplyLines) {
            line.setSupply(supply);
        }
        supply.setLines(supplyLines);
        supply = supplyRepository.save(supply);

        // Impact stock: ajouter les quantités approvisionnées
        for (SupplyLine line : supply.getLines()) {
            stockService.addStock(line.getProduct(), shop, line.getQuantity(),
                    StockReferenceType.SUPPLY, supply.getId());
        }

        // Créer les échéances si fournies
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            for (SupplyRequest.ScheduleEntry entry : request.getSchedules()) {
                PaymentSchedule schedule = PaymentSchedule.builder()
                        .referenceType(ScheduleReferenceType.SUPPLY)
                        .referenceId(supply.getId())
                        .amountDue(entry.getAmountDue())
                        .dueDate(entry.getDueDate())
                        .status(PaymentScheduleStatus.PENDING)
                        .build();
                paymentScheduleRepository.save(schedule);
            }
        } else if (status != SupplyStatus.PAID) {
            // Échéance par défaut si non payé et pas d'échéances spécifiées
            PaymentSchedule schedule = PaymentSchedule.builder()
                    .referenceType(ScheduleReferenceType.SUPPLY)
                    .referenceId(supply.getId())
                    .amountDue(totalAmount - amountPaid)
                    .dueDate(java.time.LocalDate.now().plusDays(30))
                    .status(PaymentScheduleStatus.PENDING)
                    .build();
            paymentScheduleRepository.save(schedule);
        }

        log.info("Approvisionnement créé: id={}, fournisseur={}, total={}, status={}",
                supply.getId(), supplier.getName(), totalAmount, status);
        return SupplyResponse.from(supply);
    }
}
