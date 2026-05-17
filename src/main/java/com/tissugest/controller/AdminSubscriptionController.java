package com.tissugest.controller;

import com.tissugest.dto.admin.AdminStatsResponse;
import com.tissugest.dto.admin.AssignPlanRequest;
import com.tissugest.dto.admin.SubscriptionPaymentRequest;
import com.tissugest.entity.Subscription;
import com.tissugest.entity.SubscriptionPayment;
import com.tissugest.service.AdminSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionService adminSubscriptionService;

    @GetMapping
    public ResponseEntity<List<Subscription>> listAll() {
        return ResponseEntity.ok(adminSubscriptionService.listAll());
    }

    @PostMapping
    public ResponseEntity<Subscription> assignPlan(@Valid @RequestBody AssignPlanRequest request) {
        return new ResponseEntity<>(adminSubscriptionService.assignPlan(request), HttpStatus.CREATED);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<Subscription>> expiringSoon() {
        return ResponseEntity.ok(adminSubscriptionService.listExpiringSoon());
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<SubscriptionPayment> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPaymentRequest request) {
        return new ResponseEntity<>(adminSubscriptionService.recordPayment(id, request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<SubscriptionPayment>> listPayments(@PathVariable Long id) {
        return ResponseEntity.ok(adminSubscriptionService.listPayments(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> stats() {
        return ResponseEntity.ok(adminSubscriptionService.getStats());
    }
}
