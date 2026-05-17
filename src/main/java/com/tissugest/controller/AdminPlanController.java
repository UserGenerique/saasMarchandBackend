package com.tissugest.controller;

import com.tissugest.dto.admin.PlanRequest;
import com.tissugest.dto.admin.PlanResponse;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.service.AdminPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/plans")
@RequiredArgsConstructor
public class AdminPlanController {

    private final AdminPlanService adminPlanService;

    @GetMapping
    public ResponseEntity<List<PlanResponse>> listAll() {
        return ResponseEntity.ok(adminPlanService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adminPlanService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanRequest request) {
        return new ResponseEntity<>(adminPlanService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> update(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(adminPlanService.update(id, request));
    }

    @PutMapping("/{id}/features")
    public ResponseEntity<PlanResponse> updateFeatures(@PathVariable Long id, @RequestBody List<FeatureCode> features) {
        return ResponseEntity.ok(adminPlanService.updateFeatures(id, features));
    }
}
