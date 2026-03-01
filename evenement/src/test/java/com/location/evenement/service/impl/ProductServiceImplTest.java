package com.location.evenement.service.impl;

import com.location.evenement.dto.mapper.ProductMapper;
import com.location.evenement.dto.request.ProductRequest;
import com.location.evenement.dto.response.ProductResponse;
import com.location.evenement.exception.BadRequestException;
import com.location.evenement.exception.ResourceNotFoundException;
import com.location.evenement.model.Product;
import com.location.evenement.repository.CategoryRepository;
import com.location.evenement.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ProductMapper productMapper;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        productMapper = mock(ProductMapper.class);
        service = new ProductServiceImpl(productRepository, categoryRepository, productMapper);
    }

    @Test
    void createProduct_whenReferenceAlreadyExists_throwsBadRequest() {
        ProductRequest req = mock(ProductRequest.class);
        when(req.getReference()).thenReturn("REF-1");
        when(productRepository.existsByReference("REF-1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.createProduct(req));

        verify(productRepository).existsByReference("REF-1");
        verifyNoMoreInteractions(categoryRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void createProduct_whenCategoryNotFound_throwsResourceNotFound() {
        ProductRequest req = mock(ProductRequest.class);
        when(req.getReference()).thenReturn("REF-1");
        when(req.getCategoryId()).thenReturn(10L);

        when(productRepository.existsByReference("REF-1")).thenReturn(false);
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createProduct(req));

        verify(productRepository).existsByReference("REF-1");
        verify(categoryRepository).findById(10L);
        verifyNoInteractions(productMapper);
    }

    @Test
    void addImagesToProduct_whenImagesNull_initializesAndSetsMainImage() {
        long productId = 1L;

        Product product = mock(Product.class);
        when(product.getImages()).thenReturn(null);
        when(product.getMainImage()).thenReturn(null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        ProductResponse mapped = mock(ProductResponse.class);
        when(productMapper.toResponse(product)).thenReturn(mapped);

        List<String> urls = List.of("u1.png", "u2.png");
        ProductResponse out = service.addImagesToProduct(productId, urls);

        assertSame(mapped, out);

        verify(product).setImages(any(ArrayList.class));
        verify(product).getImages();
        verify(product).setMainImage("u1.png");
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void updateStock_whenNotEnoughStock_throwsBadRequest() {
        long productId = 1L;

        Product product = mock(Product.class);
        when(product.getStockQuantity()).thenReturn(2);
        when(product.getName()).thenReturn("Produit X");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> service.updateStock(productId, 3));

        verify(productRepository, never()).save(any());
        verify(product, never()).setStockQuantity(anyInt());
        verify(product, never()).setIsAvailable(anyBoolean());
    }
}