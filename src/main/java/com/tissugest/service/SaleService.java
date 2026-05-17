package com.tissugest.service;

import com.tissugest.dto.sale.SaleRequest;
import com.tissugest.dto.sale.SaleResponse;
import com.tissugest.entity.*;
import com.tissugest.entity.enums.*;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.*;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final StockService stockService;
    private final SecurityHelper securityHelper;

    public List<SaleResponse> listByShop(LocalDate from, LocalDate to) {
        Shop shop = securityHelper.getCurrentShop();
        List<Sale> sales;
        if (from != null && to != null) {
            sales = saleRepository.findByShopIdAndSaleDateBetweenOrderByCreatedAtDesc(shop.getId(), from, to);
        } else {
            sales = saleRepository.findByShopIdOrderByCreatedAtDesc(shop.getId());
        }
        return sales.stream().map(SaleResponse::from).collect(Collectors.toList());
    }

    public SaleResponse getById(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Sale sale = saleRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Vente", id));
        return SaleResponse.from(sale);
    }

    public List<SaleResponse> listByClient(Long clientId) {
        return saleRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(SaleResponse::from)
                .collect(Collectors.toList());
    }

    public List<SaleResponse> listOpenCredits() {
        Shop shop = securityHelper.getCurrentShop();
        return saleRepository.findByShopIdAndStatus(shop.getId(), SaleStatus.CREDIT_OPEN).stream()
                .map(SaleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Crée une vente (QUICK, WITH_CLIENT ou CREDIT) avec impact stock automatique.
     */
    @Transactional
    public SaleResponse create(SaleRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Long userId = securityHelper.getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);

        // Validation du type de vente vs client
        if (request.getSaleType() == SaleType.WITH_CLIENT || request.getSaleType() == SaleType.CREDIT) {
            if (request.getClientId() == null) {
                throw BusinessException.badRequest("Un client est requis pour une vente de type " + request.getSaleType());
            }
        }

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findByIdAndShopId(request.getClientId(), shop.getId())
                    .orElseThrow(() -> BusinessException.notFound("Client", request.getClientId()));
        }

        // Construire les lignes et calculer les totaux
        List<SaleLine> saleLines = new ArrayList<>();
        long totalAmount = 0;
        long totalDiscount = 0;

        for (SaleRequest.SaleLineRequest lineReq : request.getLines()) {
            Product product = productRepository.findByIdAndShopId(lineReq.getProductId(), shop.getId())
                    .orElseThrow(() -> BusinessException.notFound("Produit", lineReq.getProductId()));

            long unitPrice = lineReq.getUnitPrice() != null ? lineReq.getUnitPrice() : product.getSellingPrice();
            long discount = lineReq.getDiscount() != null ? lineReq.getDiscount() : 0L;

            // lineTotal = (unitPrice * quantity) - discount
            long lineGross = unitPrice * lineReq.getQuantity().longValue();
            long lineTotal = Math.max(0, lineGross - discount);

            SaleLine saleLine = SaleLine.builder()
                    .product(product)
                    .quantity(lineReq.getQuantity())
                    .unitPrice(unitPrice)
                    .discount(discount)
                    .lineTotal(lineTotal)
                    .build();
            saleLines.add(saleLine);

            totalAmount += lineTotal;
            totalDiscount += discount;
        }

        // Déterminer le montant payé et restant
        long amountPaid;
        SaleStatus status;

        switch (request.getSaleType()) {
            case QUICK:
            case WITH_CLIENT:
                amountPaid = totalAmount;
                status = SaleStatus.COMPLETED;
                break;
            case CREDIT:
                amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : 0L;
                if (amountPaid >= totalAmount) {
                    status = SaleStatus.COMPLETED;
                    amountPaid = totalAmount;
                } else {
                    status = SaleStatus.CREDIT_OPEN;
                }
                break;
            default:
                throw BusinessException.badRequest("Type de vente non supporté");
        }

        long remainingAmount = totalAmount - amountPaid;

        Sale sale = Sale.builder()
                .shop(shop)
                .client(client)
                .saleType(request.getSaleType())
                .totalAmount(totalAmount)
                .discountAmount(totalDiscount)
                .amountPaid(amountPaid)
                .remainingAmount(remainingAmount)
                .status(status)
                .saleDate(request.getSaleDate() != null ? request.getSaleDate() : LocalDate.now())
                .createdBy(user)
                .build();
        sale = saleRepository.save(sale);

        // Sauvegarder les lignes avec la référence sale
        for (SaleLine line : saleLines) {
            line.setSale(sale);
        }
        sale.setLines(saleLines);
        sale = saleRepository.save(sale);

        // Impact stock: retirer les quantités vendues
        for (SaleLine line : sale.getLines()) {
            stockService.removeStock(line.getProduct(), shop, line.getQuantity(),
                    StockReferenceType.SALE, sale.getId());
        }

        // Créer une échéance si vente à crédit avec solde restant
        if (status == SaleStatus.CREDIT_OPEN && remainingAmount > 0) {
            PaymentSchedule schedule = PaymentSchedule.builder()
                    .referenceType(ScheduleReferenceType.SALE)
                    .referenceId(sale.getId())
                    .amountDue(remainingAmount)
                    .dueDate(LocalDate.now().plusDays(30)) // échéance par défaut: 30 jours
                    .status(PaymentScheduleStatus.PENDING)
                    .build();
            paymentScheduleRepository.save(schedule);
        }

        log.info("Vente créée: id={}, type={}, total={}, status={}", sale.getId(), sale.getSaleType(), totalAmount, status);
        return SaleResponse.from(sale);
    }

    /**
     * Annulation simple de vente avec restauration du stock.
     */
    @Transactional
    public SaleResponse cancel(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Sale sale = saleRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Vente", id));

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw BusinessException.badRequest("Cette vente est déjà annulée");
        }

        // Restaurer le stock pour chaque ligne
        for (SaleLine line : sale.getLines()) {
            stockService.restoreStock(line.getProduct(), shop, line.getQuantity(), sale.getId());
        }

        sale.setStatus(SaleStatus.CANCELLED);
        sale = saleRepository.save(sale);

        log.info("Vente annulée: id={}, stock restauré", sale.getId());
        return SaleResponse.from(sale);
    }

    /**
     * Stats du jour.
     */
    public DailySummary getDailySummary(LocalDate date) {
        Shop shop = securityHelper.getCurrentShop();
        LocalDate targetDate = date != null ? date : LocalDate.now();
        Long totalSales = saleRepository.sumSalesByDate(shop.getId(), targetDate);
        Long countSales = saleRepository.countSalesByDate(shop.getId(), targetDate);
        return new DailySummary(targetDate, totalSales, countSales);
    }

    public record DailySummary(LocalDate date, Long totalAmount, Long count) {}
}
