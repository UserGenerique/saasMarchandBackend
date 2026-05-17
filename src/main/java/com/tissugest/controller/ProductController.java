package com.tissugest.controller;

import com.tissugest.dto.product.ProductRequest;
import com.tissugest.dto.product.ProductResponse;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<List<ProductResponse>> list(@RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(productService.listByShop(categoryId));
    }

    @GetMapping("/{id}")
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<List<ProductResponse>> lowStock() {
        return ResponseEntity.ok(productService.getLowStock());
    }
}
