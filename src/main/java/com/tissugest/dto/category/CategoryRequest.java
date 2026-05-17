package com.tissugest.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String name;
    private String icon;
}
