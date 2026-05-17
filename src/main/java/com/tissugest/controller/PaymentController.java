package com.tissugest.controller;

import com.tissugest.dto.payment.ClientPaymentRequest;
import com.tissugest.dto.payment.SupplierPaymentRequest;
import com.tissugest.entity.ClientPayment;
import com.tissugest.entity.SupplierPayment;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.ClientPaymentService;
import com.tissugest.service.SupplierPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ClientPaymentService clientPaymentService;
    private final SupplierPaymentService supplierPaymentService;

    // --- Paiements clients ---

    @PostMapping("/clients")
    @RequiresFeature(FeatureCode.CREDITS)
    public ResponseEntity<ClientPayment> createClientPayment(@Valid @RequestBody ClientPaymentRequest request) {
        return new ResponseEntity<>(clientPaymentService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/clients/by-sale/{saleId}")
    @RequiresFeature(FeatureCode.CREDITS)
    public ResponseEntity<List<ClientPayment>> clientPaymentsBySale(@PathVariable Long saleId) {
        return ResponseEntity.ok(clientPaymentService.listBySale(saleId));
    }

    @GetMapping("/clients/by-client/{clientId}")
    @RequiresFeature(FeatureCode.CREDITS)
    public ResponseEntity<List<ClientPayment>> clientPaymentsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientPaymentService.listByClient(clientId));
    }

    // --- Paiements fournisseurs ---

    @PostMapping("/suppliers")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<SupplierPayment> createSupplierPayment(@Valid @RequestBody SupplierPaymentRequest request) {
        return new ResponseEntity<>(supplierPaymentService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/suppliers/by-supply/{supplyId}")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<List<SupplierPayment>> supplierPaymentsBySupply(@PathVariable Long supplyId) {
        return ResponseEntity.ok(supplierPaymentService.listBySupply(supplyId));
    }

    @GetMapping("/suppliers/by-supplier/{supplierId}")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<List<SupplierPayment>> supplierPaymentsBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierPaymentService.listBySupplier(supplierId));
    }
}
