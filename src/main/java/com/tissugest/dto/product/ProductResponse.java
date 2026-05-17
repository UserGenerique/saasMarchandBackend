package com.tissugest.dto.product;

import com.tissugest.entity.Product;
import com.tissugest.entity.enums.ProductUnit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long purchasePrice;
    private Long sellingPrice;
    private ProductUnit unit;
    private String photoUrl;
    private Integer lowStockThreshold;
    private BigDecimal stockQuantity;
    private boolean lowStock;

    public static ProductResponse from(Product p) {
        BigDecimal qty = p.getStock() != null ? p.getStock().getQuantity() : BigDecimal.ZERO;
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .purchasePrice(p.getPurchasePrice())
                .sellingPrice(p.getSellingPrice())
                .unit(p.getUnit())
                .photoUrl(p.getPhotoUrl())
                .lowStockThreshold(p.getLowStockThreshold())
                .stockQuantity(qty)
                .lowStock(qty.compareTo(BigDecimal.valueOf(p.getLowStockThreshold())) <= 0)
                .build();
    }
}
