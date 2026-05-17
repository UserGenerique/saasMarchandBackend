package com.tissugest.service;

import com.tissugest.dto.product.ProductRequest;
import com.tissugest.dto.product.ProductResponse;
import com.tissugest.entity.Category;
import com.tissugest.entity.Product;
import com.tissugest.entity.Shop;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.CategoryRepository;
import com.tissugest.repository.ProductRepository;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockService stockService;
    private final SecurityHelper securityHelper;

    public List<ProductResponse> listByShop(Long categoryId) {
        Shop shop = securityHelper.getCurrentShop();
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByShopIdAndCategoryIdAndIsActiveTrue(shop.getId(), categoryId);
        } else {
            products = productRepository.findByShopIdAndIsActiveTrue(shop.getId());
        }
        return products.stream().map(ProductResponse::from).collect(Collectors.toList());
    }

    public ProductResponse getById(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Product product = productRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Produit", id));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndShopId(request.getCategoryId(), shop.getId())
                    .orElseThrow(() -> BusinessException.notFound("Catégorie", request.getCategoryId()));
        }

        Product product = Product.builder()
                .shop(shop)
                .category(category)
                .name(request.getName())
                .purchasePrice(request.getPurchasePrice())
                .sellingPrice(request.getSellingPrice())
                .unit(request.getUnit())
                .photoUrl(request.getPhotoUrl())
                .lowStockThreshold(request.getLowStockThreshold())
                .build();
        product = productRepository.save(product);

        // Auto-création du stock initial
        stockService.initializeStock(product);

        // Recharger pour inclure le stock dans la réponse
        product = productRepository.findById(product.getId()).orElseThrow();
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Product product = productRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Produit", id));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndShopId(request.getCategoryId(), shop.getId())
                    .orElseThrow(() -> BusinessException.notFound("Catégorie", request.getCategoryId()));
        }

        product.setCategory(category);
        product.setName(request.getName());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setUnit(request.getUnit());
        product.setPhotoUrl(request.getPhotoUrl());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product = productRepository.save(product);
        return ProductResponse.from(product);
    }

    @Transactional
    public void deactivate(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Product product = productRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Produit", id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    public List<ProductResponse> getLowStock() {
        Shop shop = securityHelper.getCurrentShop();
        return productRepository.findLowStockProducts(shop.getId()).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
}
