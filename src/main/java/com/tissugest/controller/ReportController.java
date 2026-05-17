package com.tissugest.controller;

import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily-summary")
    @RequiresFeature(FeatureCode.REPORTS)
    public ResponseEntity<ReportService.DailySummary> dailySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.getDailySummary(date));
    }

    @GetMapping("/sales")
    @RequiresFeature(FeatureCode.REPORTS)
    public ResponseEntity<ReportService.PeriodSummary> salesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getSalesByPeriod(from, to));
    }

    @GetMapping("/top-products")
    @RequiresFeature(FeatureCode.REPORTS)
    public ResponseEntity<List<ReportService.TopProduct>> topProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getTopProducts(from, to, limit));
    }

    @GetMapping("/debts-clients")
    @RequiresFeature(FeatureCode.REPORTS)
    public ResponseEntity<List<ReportService.ClientDebt>> clientDebts() {
        return ResponseEntity.ok(reportService.getClientDebts());
    }

    @GetMapping("/debts-suppliers")
    @RequiresFeature(FeatureCode.REPORTS)
    public ResponseEntity<List<ReportService.SupplierDebt>> supplierDebts() {
        return ResponseEntity.ok(reportService.getSupplierDebts());
    }

    @GetMapping("/stock-valuation")
    @RequiresFeature(FeatureCode.REPORTS)
    public ResponseEntity<ReportService.StockValuation> stockValuation() {
        return ResponseEntity.ok(reportService.getStockValuation());
    }
}
