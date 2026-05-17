package com.tissugest.controller;

import com.tissugest.entity.PaymentSchedule;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.entity.enums.ScheduleReferenceType;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/pending")
    @RequiresFeature(FeatureCode.CREDITS)
    public ResponseEntity<List<PaymentSchedule>> pendingAndOverdue() {
        return ResponseEntity.ok(scheduleService.listPendingAndOverdue());
    }

    @GetMapping("/by-sale/{saleId}")
    @RequiresFeature(FeatureCode.CREDITS)
    public ResponseEntity<List<PaymentSchedule>> bySale(@PathVariable Long saleId) {
        return ResponseEntity.ok(scheduleService.listByReference(ScheduleReferenceType.SALE, saleId));
    }

    @GetMapping("/by-supply/{supplyId}")
    @RequiresFeature(FeatureCode.SUPPLIERS)
    public ResponseEntity<List<PaymentSchedule>> bySupply(@PathVariable Long supplyId) {
        return ResponseEntity.ok(scheduleService.listByReference(ScheduleReferenceType.SUPPLY, supplyId));
    }

    @PostMapping("/{id}/mark-paid")
    @RequiresFeature(FeatureCode.CREDITS)
    public ResponseEntity<PaymentSchedule> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.markAsPaid(id));
    }
}
