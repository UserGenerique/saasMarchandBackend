package com.tissugest.dto.supplier;

import com.tissugest.entity.Supplier;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String notes;
    private Long totalDebt;

    public static SupplierResponse from(Supplier s, Long debt) {
        return SupplierResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .phone(s.getPhone())
                .address(s.getAddress())
                .notes(s.getNotes())
                .totalDebt(debt != null ? debt : 0L)
                .build();
    }

    public static SupplierResponse from(Supplier s) {
        return from(s, 0L);
    }
}
