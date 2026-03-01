package com.location.evenement.dto.mapper;

import com.location.evenement.dto.request.ProductRequest;
import com.location.evenement.dto.response.ProductResponse;
import com.location.evenement.model.Category;
import com.location.evenement.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request, Category category) {
        Product product = new Product();
        product.setName(request.getName());
        product.setReference(request.getReference());
        product.setDescription(request.getDescription());
        product.setPricePerDay(request.getPricePerDay());
        product.setStockQuantity(request.getStockQuantity());
        product.setIsAvailable(request.getIsAvailable());
        product.setCategory(category);

        // Gérer les URLs d'images (peuvent être des liens externes)
        if (request.getImageUrls() != null) {
            product.setImages(request.getImageUrls());
            product.setMainImage(request.getMainImage());
        }

        return product;
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setReference(product.getReference());
        response.setDescription(product.getDescription());
        response.setPricePerDay(product.getPricePerDay());
        response.setStockQuantity(product.getStockQuantity());
        response.setIsAvailable(product.getIsAvailable());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setImages(product.getImages());
        response.setMainImage(product.getMainImage());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    public void updateEntity(Product product, ProductRequest request, Category category) {
        if (request.getName() != null) product.setName(request.getName());
        if (request.getReference() != null) product.setReference(request.getReference());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPricePerDay() != null) product.setPricePerDay(request.getPricePerDay());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        if (request.getIsAvailable() != null) product.setIsAvailable(request.getIsAvailable());
        if (category != null) product.setCategory(category);

        // Mettre à jour les images si fournies
        if (request.getImageUrls() != null) {
            // Fusionner avec les images existantes ou remplacer ?
            // Ici on remplace, mais vous pouvez choisir de fusionner
            product.setImages(request.getImageUrls());
            product.setMainImage(request.getMainImage());
        }
    }
}