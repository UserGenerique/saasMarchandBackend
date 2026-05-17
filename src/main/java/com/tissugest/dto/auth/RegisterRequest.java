package com.tissugest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Size(min = 8, max = 20, message = "Le numéro doit contenir entre 8 et 20 caractères")
    private String phone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 4, max = 100, message = "Le mot de passe doit contenir au moins 4 caractères")
    private String password;

    @NotBlank(message = "Le nom complet est obligatoire")
    private String fullName;

    @NotBlank(message = "Le nom de la boutique est obligatoire")
    private String businessName;

    private String email;
    private String address;
}
