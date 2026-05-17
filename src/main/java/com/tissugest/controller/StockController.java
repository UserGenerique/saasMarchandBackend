package com.tissugest.controller;

import com.tissugest.dto.stock.StockAdjustRequest;
import com.tissugest.entity.Product;
import com.tissugest.entity.Shop;
import com.tissugest.entity.StockMovement;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.ProductRepository;
import com.tissugest.repository.StockMovementRepository;
import com.tissugest.security.RequiresFeature;
import com.tissugest.security.SecurityHelper;
import com.tissugest.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final SecurityHelper securityHelper;

    @PostMapping("/adjust/{productId}")
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<Void> adjustStock(@PathVariable Long productId,
                                            @Valid @RequestBody StockAdjustRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Product product = productRepository.findByIdAndShopId(productId, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Produit", productId));
        stockService.adjustStock(product, shop, request.getNewQuantity(), request.getNote());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/movements/{productId}")
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<List<StockMovement>> movements(@PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }
}
