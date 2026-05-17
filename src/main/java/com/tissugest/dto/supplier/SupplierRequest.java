package com.tissugest.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {
    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    private String name;
    private String phone;
    private String address;
    private String notes;
}
