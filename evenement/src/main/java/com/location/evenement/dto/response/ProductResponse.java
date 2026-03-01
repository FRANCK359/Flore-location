package com.location.evenement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String reference;
    private String description;
    private BigDecimal pricePerDay;
    private Integer stockQuantity;
    private Boolean isAvailable;
    private Long categoryId;
    private String categoryName;
    private List<String> images;
    private String mainImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}