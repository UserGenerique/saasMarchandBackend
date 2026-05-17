package com.tissugest.controller;

import com.tissugest.dto.supply.SupplyRequest;
import com.tissugest.dto.supply.SupplyResponse;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.SupplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;

    @GetMapping
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<List<SupplyResponse>> list() {
        return ResponseEntity.ok(supplyService.listByShop());
    }

    @GetMapping("/{id}")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<SupplyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplyService.getById(id));
    }

    @PostMapping
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<SupplyResponse> create(@Valid @RequestBody SupplyRequest request) {
        return new ResponseEntity<>(supplyService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/by-supplier/{supplierId}")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<List<SupplyResponse>> bySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplyService.listBySupplier(supplierId));
    }
}
