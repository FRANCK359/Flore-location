package com.location.evenement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String name;

    private String description;
    private String imageUrl;
    private Integer displayOrder;
}