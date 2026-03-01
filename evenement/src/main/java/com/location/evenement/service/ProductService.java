package com.location.evenement.service;

import com.location.evenement.dto.request.ProductRequest;
import com.location.evenement.dto.response.ProductResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProductById(Long id);
    ProductResponse getProductByReference(String reference);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getProductsByCategory(Long categoryId);
    List<ProductResponse> getAvailableProducts();
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    boolean checkStockAvailability(Long productId, Integer quantity);

    // Ajouter cette méthode dans ProductServiceImpl
    @Transactional
    ProductResponse addImagesToProduct(Long productId, List<String> imageUrls);

    void updateStock(Long productId, Integer quantity);
}