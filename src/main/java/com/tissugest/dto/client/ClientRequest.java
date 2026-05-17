package com.tissugest.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank(message = "Le nom du client est obligatoire")
    private String name;
    private String phone;
    private String notes;
}
