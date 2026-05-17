package com.tissugest.service;

import com.tissugest.entity.Client;
import com.tissugest.entity.Product;
import com.tissugest.entity.Shop;
import com.tissugest.entity.Supplier;
import com.tissugest.repository.*;
import com.tissugest.security.SecurityHelper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final SupplierRepository supplierRepository;
    private final SupplyRepository supplyRepository;
    private final SecurityHelper securityHelper;

    /**
     * Résumé journalier : ventes + nombre.
     */
    public DailySummary getDailySummary(LocalDate date) {
        Shop shop = securityHelper.getCurrentShop();
        LocalDate targetDate = date != null ? date : LocalDate.now();
        Long total = saleRepository.sumSalesByDate(shop.getId(), targetDate);
        Long count = saleRepository.countSalesByDate(shop.getId(), targetDate);
        return new DailySummary(targetDate, total, count);
    }

    /**
     * Ventes par période.
     */
    public PeriodSummary getSalesByPeriod(LocalDate from, LocalDate to) {
        Shop shop = securityHelper.getCurrentShop();
        var sales = saleRepository.findByShopIdAndSaleDateBetweenOrderByCreatedAtDesc(shop.getId(), from, to);
        long totalAmount = sales.stream()
                .filter(s -> s.getStatus() != com.tissugest.entity.enums.SaleStatus.CANCELLED)
                .mapToLong(s -> s.getTotalAmount())
                .sum();
        long count = sales.stream()
                .filter(s -> s.getStatus() != com.tissugest.entity.enums.SaleStatus.CANCELLED)
                .count();
        return new PeriodSummary(from, to, totalAmount, count);
    }

    /**
     * Top produits vendus (par quantité totale vendue).
     */
    public List<TopProduct> getTopProducts(LocalDate from, LocalDate to, int limit) {
        Shop shop = securityHelper.getCurrentShop();
        var sales = saleRepository.findByShopIdAndSaleDateBetweenOrderByCreatedAtDesc(shop.getId(), from, to);

        // Agréger par produit
        var productTotals = sales.stream()
                .filter(s -> s.getStatus() != com.tissugest.entity.enums.SaleStatus.CANCELLED)
                .flatMap(s -> s.getLines().stream())
                .collect(Collectors.groupingBy(
                        l -> l.getProduct().getId(),
                        Collectors.summingLong(l -> l.getLineTotal())
                ));

        return productTotals.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> {
                    Product p = productRepository.findById(e.getKey()).orElse(null);
                    return TopProduct.builder()
                            .productId(e.getKey())
                            .productName(p != null ? p.getName() : "?")
                            .totalRevenue(e.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Créances clients (tous les clients avec solde > 0).
     */
    public List<ClientDebt> getClientDebts() {
        Shop shop = securityHelper.getCurrentShop();
        return clientRepository.findByShopId(shop.getId()).stream()
                .map(c -> {
                    Long debt = saleRepository.calculateClientDebt(c.getId());
                    return ClientDebt.builder()
                            .clientId(c.getId())
                            .clientName(c.getName())
                            .phone(c.getPhone())
                            .totalDebt(debt)
                            .build();
                })
                .filter(d -> d.getTotalDebt() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Dettes fournisseurs.
     */
    public List<SupplierDebt> getSupplierDebts() {
        Shop shop = securityHelper.getCurrentShop();
        return supplierRepository.findByShopId(shop.getId()).stream()
                .map(s -> {
                    Long debt = supplyRepository.calculateDebtToSupplier(s.getId());
                    return SupplierDebt.builder()
                            .supplierId(s.getId())
                            .supplierName(s.getName())
                            .phone(s.getPhone())
                            .totalDebt(debt)
                            .build();
                })
                .filter(d -> d.getTotalDebt() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Stock valorisé (somme quantité × prix d'achat).
     */
    public StockValuation getStockValuation() {
        Shop shop = securityHelper.getCurrentShop();
        List<Product> products = productRepository.findByShopIdAndIsActiveTrue(shop.getId());
        long totalValue = 0;
        int totalProducts = products.size();
        int lowStockCount = 0;

        for (Product p : products) {
            if (p.getStock() != null) {
                long qty = p.getStock().getQuantity().longValue();
                totalValue += qty * p.getPurchasePrice();
                if (p.getStock().getQuantity().compareTo(java.math.BigDecimal.valueOf(p.getLowStockThreshold())) <= 0) {
                    lowStockCount++;
                }
            }
        }

        return StockValuation.builder()
                .totalProducts(totalProducts)
                .totalValue(totalValue)
                .lowStockCount(lowStockCount)
                .build();
    }

    // --- Record types ---
    public record DailySummary(LocalDate date, Long totalAmount, Long count) {}
    public record PeriodSummary(LocalDate from, LocalDate to, long totalAmount, long count) {}

    @Data @Builder
    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long totalRevenue;
    }

    @Data @Builder
    public static class ClientDebt {
        private Long clientId;
        private String clientName;
        private String phone;
        private Long totalDebt;
    }

    @Data @Builder
    public static class SupplierDebt {
        private Long supplierId;
        private String supplierName;
        private String phone;
        private Long totalDebt;
    }

    @Data @Builder
    public static class StockValuation {
        private int totalProducts;
        private long totalValue;
        private int lowStockCount;
    }
}
