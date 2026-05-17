package com.tissugest.dto.product;

import com.tissugest.entity.enums.ProductUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Le nom du produit est obligatoire")
    private String name;
    private Long categoryId;
    @NotNull(message = "Le prix d'achat est obligatoire")
    private Long purchasePrice;
    @NotNull(message = "Le prix de vente est obligatoire")
    private Long sellingPrice;
    private ProductUnit unit = ProductUnit.PIECE;
    private String photoUrl;
    private Integer lowStockThreshold = 5;
}
