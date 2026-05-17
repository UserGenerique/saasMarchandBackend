package com.tissugest.service;

import com.tissugest.dto.supplier.SupplierRequest;
import com.tissugest.dto.supplier.SupplierResponse;
import com.tissugest.entity.Shop;
import com.tissugest.entity.Supplier;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.SupplierRepository;
import com.tissugest.repository.SupplyRepository;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplyRepository supplyRepository;
    private final SecurityHelper securityHelper;

    public List<SupplierResponse> listByShop() {
        Shop shop = securityHelper.getCurrentShop();
        return supplierRepository.findByShopId(shop.getId()).stream()
                .map(s -> SupplierResponse.from(s, supplyRepository.calculateDebtToSupplier(s.getId())))
                .collect(Collectors.toList());
    }

    public SupplierResponse getById(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Supplier supplier = supplierRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Fournisseur", id));
        Long debt = supplyRepository.calculateDebtToSupplier(supplier.getId());
        return SupplierResponse.from(supplier, debt);
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Supplier supplier = Supplier.builder()
                .shop(shop)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .notes(request.getNotes())
                .build();
        supplier = supplierRepository.save(supplier);
        return SupplierResponse.from(supplier, 0L);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Supplier supplier = supplierRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Fournisseur", id));
        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setNotes(request.getNotes());
        supplier = supplierRepository.save(supplier);
        Long debt = supplyRepository.calculateDebtToSupplier(supplier.getId());
        return SupplierResponse.from(supplier, debt);
    }

    @Transactional
    public void delete(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Supplier supplier = supplierRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Fournisseur", id));
        supplierRepository.delete(supplier);
    }
}
