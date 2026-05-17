package com.tissugest.controller;

import com.tissugest.dto.admin.AdminMerchantResponse;
import com.tissugest.service.AdminMerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @GetMapping
    public ResponseEntity<List<AdminMerchantResponse>> listAll() {
        return ResponseEntity.ok(adminMerchantService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminMerchantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adminMerchantService.getById(id));
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<AdminMerchantResponse> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(adminMerchantService.suspend(id));
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<AdminMerchantResponse> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(adminMerchantService.reactivate(id));
    }
}
