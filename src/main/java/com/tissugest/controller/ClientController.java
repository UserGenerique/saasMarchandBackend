package com.tissugest.controller;

import com.tissugest.dto.client.ClientRequest;
import com.tissugest.dto.client.ClientResponse;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @RequiresFeature(FeatureCode.CLIENTS)
    public ResponseEntity<List<ClientResponse>> list() {
        return ResponseEntity.ok(clientService.listByShop());
    }

    @GetMapping("/{id}")
    @RequiresFeature(FeatureCode.CLIENTS)
    public ResponseEntity<ClientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @PostMapping
    @RequiresFeature(FeatureCode.CLIENTS)
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        return new ResponseEntity<>(clientService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RequiresFeature(FeatureCode.CLIENTS)
    public ResponseEntity<ClientResponse> update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresFeature(FeatureCode.CLIENTS)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
