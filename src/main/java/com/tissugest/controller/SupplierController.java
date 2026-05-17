package com.tissugest.controller;

import com.tissugest.dto.supplier.SupplierRequest;
import com.tissugest.dto.supplier.SupplierResponse;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<List<SupplierResponse>> list() {
        return ResponseEntity.ok(supplierService.listByShop());
    }

    @GetMapping("/{id}")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @PostMapping
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return new ResponseEntity<>(supplierService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
