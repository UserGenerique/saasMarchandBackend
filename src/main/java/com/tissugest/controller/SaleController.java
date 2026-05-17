package com.tissugest.controller;

import com.tissugest.dto.sale.SaleRequest;
import com.tissugest.dto.sale.SaleResponse;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    @RequiresFeature(FeatureCode.SALES)
    public ResponseEntity<List<SaleResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(saleService.listByShop(from, to));
    }

    @GetMapping("/{id}")
    @RequiresFeature(FeatureCode.SALES)
    public ResponseEntity<SaleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @PostMapping
    @RequiresFeature(FeatureCode.SALES)
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest request) {
        return new ResponseEntity<>(saleService.create(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/cancel")
    @RequiresFeature(FeatureCode.SALES)
    public ResponseEntity<SaleResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.cancel(id));
    }

    @GetMapping("/credits")
    @RequiresFeature(FeatureCode.CREDITS)
    public ResponseEntity<List<SaleResponse>> openCredits() {
        return ResponseEntity.ok(saleService.listOpenCredits());
    }

    @GetMapping("/by-client/{clientId}")
    @RequiresFeature(FeatureCode.SALES)
    public ResponseEntity<List<SaleResponse>> byClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(saleService.listByClient(clientId));
    }

    @GetMapping("/daily-summary")
    @RequiresFeature(FeatureCode.SALES)
    public ResponseEntity<SaleService.DailySummary> dailySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(saleService.getDailySummary(date));
    }
}
