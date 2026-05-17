package com.tissugest.service;

import com.tissugest.entity.*;
import com.tissugest.entity.enums.StockMovementType;
import com.tissugest.entity.enums.StockReferenceType;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.StockMovementRepository;
import com.tissugest.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;

    /**
     * Crée l'enregistrement de stock initial pour un nouveau produit.
     */
    @Transactional
    public Stock initializeStock(Product product) {
        Stock stock = Stock.builder()
                .product(product)
                .quantity(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();
        return stockRepository.save(stock);
    }

    /**
     * Ajoute du stock (approvisionnement).
     */
    @Transactional
    public void addStock(Product product, Shop shop, BigDecimal quantity, StockReferenceType refType, Long refId) {
        Stock stock = getOrCreateStock(product);
        stock.setQuantity(stock.getQuantity().add(quantity));
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        createMovement(product, shop, StockMovementType.IN, quantity, refType, refId, null);
        log.debug("Stock IN: product={}, qty={}, new_total={}", product.getId(), quantity, stock.getQuantity());
    }

    /**
     * Retire du stock (vente).
     */
    @Transactional
    public void removeStock(Product product, Shop shop, BigDecimal quantity, StockReferenceType refType, Long refId) {
        Stock stock = getOrCreateStock(product);

        if (stock.getQuantity().compareTo(quantity) < 0) {
            throw BusinessException.badRequest("Stock insuffisant pour " + product.getName()
                    + " (disponible: " + stock.getQuantity() + ", demandé: " + quantity + ")");
        }

        stock.setQuantity(stock.getQuantity().subtract(quantity));
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        createMovement(product, shop, StockMovementType.OUT, quantity, refType, refId, null);
        log.debug("Stock OUT: product={}, qty={}, new_total={}", product.getId(), quantity, stock.getQuantity());
    }

    /**
     * Restaure le stock (annulation de vente).
     */
    @Transactional
    public void restoreStock(Product product, Shop shop, BigDecimal quantity, Long saleId) {
        Stock stock = getOrCreateStock(product);
        stock.setQuantity(stock.getQuantity().add(quantity));
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        createMovement(product, shop, StockMovementType.IN, quantity, StockReferenceType.CANCELLATION, saleId, "Annulation vente");
    }

    /**
     * Ajustement manuel du stock.
     */
    @Transactional
    public void adjustStock(Product product, Shop shop, BigDecimal newQuantity, String note) {
        Stock stock = getOrCreateStock(product);
        BigDecimal diff = newQuantity.subtract(stock.getQuantity());

        stock.setQuantity(newQuantity);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        StockMovementType type = diff.compareTo(BigDecimal.ZERO) >= 0 ? StockMovementType.IN : StockMovementType.OUT;
        createMovement(product, shop, type, diff.abs(), StockReferenceType.MANUAL, null, note);
    }

    private Stock getOrCreateStock(Product product) {
        return stockRepository.findByProductId(product.getId())
                .orElseGet(() -> initializeStock(product));
    }

    private void createMovement(Product product, Shop shop, StockMovementType type,
                                BigDecimal quantity, StockReferenceType refType, Long refId, String note) {
        StockMovement movement = StockMovement.builder()
                .product(product)
                .shop(shop)
                .type(type)
                .quantity(quantity)
                .referenceType(refType)
                .referenceId(refId)
                .note(note)
                .build();
        stockMovementRepository.save(movement);
    }
}
