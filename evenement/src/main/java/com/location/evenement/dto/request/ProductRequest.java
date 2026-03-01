package com.location.evenement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    private String description;

    @NotNull(message = "Le prix par jour est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal pricePerDay;

    @NotNull(message = "La quantité en stock est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    private Integer stockQuantity;

    private Boolean isAvailable = true;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    // Pour les URLs d'images externes
    private List<String> imageUrls;

    // Pour l'image principale (URL externe ou chemin local)
    private String mainImage;
}