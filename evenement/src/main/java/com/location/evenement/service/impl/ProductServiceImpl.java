package com.location.evenement.service.impl;

import com.location.evenement.dto.request.ProductRequest;
import com.location.evenement.dto.response.ProductResponse;
import com.location.evenement.dto.mapper.ProductMapper;
import com.location.evenement.exception.BadRequestException;
import com.location.evenement.exception.ResourceNotFoundException;
import com.location.evenement.model.Category;
import com.location.evenement.model.Product;
import com.location.evenement.repository.CategoryRepository;
import com.location.evenement.repository.ProductRepository;
import com.location.evenement.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByReference(request.getReference())) {
            throw new BadRequestException("Un produit avec la référence '" + request.getReference() + "' existe déjà");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'id: " + request.getCategoryId()));

        Product product = productMapper.toEntity(request, category);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse getProductByReference(String reference) {
        Product product = productRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec la référence: " + reference));
        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAvailableProducts() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + id));

        if (request.getReference() != null && !request.getReference().equals(product.getReference())) {
            if (productRepository.existsByReferenceAndIdNot(request.getReference(), id)) {
                throw new BadRequestException("Un autre produit avec la référence '" + request.getReference() + "' existe déjà");
            }
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'id: " + request.getCategoryId()));
        }

        productMapper.updateEntity(product, request, category);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produit non trouvé avec l'id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public boolean checkStockAvailability(Long productId, Integer quantity) {
        Product product = getProductByIdEntity(productId);
        return product.getIsAvailable() && product.getStockQuantity() >= quantity;
    }

    // Ajouter cette méthode dans ProductServiceImpl
    @Transactional
    @Override
    public ProductResponse addImagesToProduct(Long productId, List<String> imageUrls) {
        Product product = getProductByIdEntity(productId);

        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }

        product.getImages().addAll(imageUrls);

        if (product.getMainImage() == null && !imageUrls.isEmpty()) {
            product.setMainImage(imageUrls.get(0));
        }

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        Product product = getProductByIdEntity(productId);

        int newStock = product.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new BadRequestException("Stock insuffisant pour le produit: " + product.getName());
        }

        product.setStockQuantity(newStock);
        product.setIsAvailable(newStock > 0);
        productRepository.save(product);
    }

    private Product getProductByIdEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + id));
    }
}