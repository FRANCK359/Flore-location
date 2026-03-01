package com.location.evenement.controller;

import com.location.evenement.dto.request.ProductImageUploadRequest;
import com.location.evenement.dto.response.ProductResponse;
import com.location.evenement.dto.response.UploadResponse;
import com.location.evenement.exception.ResourceNotFoundException;
import com.location.evenement.model.Product;
import com.location.evenement.repository.ProductRepository;
import com.location.evenement.service.FileStorageService;
import com.location.evenement.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    /**
     * Upload d'images depuis le PC
     */
    @PostMapping(value = "/{productId}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UploadResponse> uploadProductImages(
            @PathVariable Long productId,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "setAsMain", defaultValue = "true") Boolean setAsMain) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + productId));

        try {
            // Sauvegarder les fichiers uploadés
            List<String> imageUrls = fileStorageService.storeMultipleFiles(images, productId);

            // Ajouter les URLs au produit
            List<String> existingImages = product.getImages();
            if (existingImages == null) {
                existingImages = new ArrayList<>();
            }
            existingImages.addAll(imageUrls);
            product.setImages(existingImages);

            // Définir l'image principale si demandé et si c'est la première image
            if (setAsMain && (product.getMainImage() == null || product.getMainImage().isEmpty())) {
                product.setMainImage(imageUrls.get(0));
            }

            productRepository.save(product);

            log.info("{} image(s) uploadée(s) pour le produit {}", imageUrls.size(), productId);

            return ResponseEntity.ok(new UploadResponse(
                    imageUrls,
                    imageUrls.size() + " image(s) uploadée(s) avec succès",
                    true
            ));

        } catch (IOException e) {
            log.error("Erreur lors de l'upload des images", e);
            return ResponseEntity.badRequest().body(new UploadResponse(
                    null,
                    "Erreur lors de l'upload: " + e.getMessage(),
                    false
            ));
        }
    }

    /**
     * Ajout d'images via URLs externes
     */
    @PostMapping("/{productId}/images/urls")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UploadResponse> addImageUrls(
            @PathVariable Long productId,
            @RequestBody List<String> imageUrls) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + productId));

        // Ajouter les URLs au produit
        List<String> existingImages = product.getImages();
        if (existingImages == null) {
            existingImages = new ArrayList<>();
        }
        existingImages.addAll(imageUrls);
        product.setImages(existingImages);

        // Si pas d'image principale, prendre la première URL
        if (product.getMainImage() == null && !imageUrls.isEmpty()) {
            product.setMainImage(imageUrls.get(0));
        }

        productRepository.save(product);

        return ResponseEntity.ok(new UploadResponse(
                imageUrls,
                imageUrls.size() + " URL(s) d'image ajoutée(s) avec succès",
                true
        ));
    }

    /**
     * Upload d'une seule image (pour l'image principale)
     */
    @PostMapping(value = "/{productId}/images/main/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> uploadMainImage(
            @PathVariable Long productId,
            @RequestParam("image") MultipartFile image) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + productId));

        try {
            // Sauvegarder le fichier
            String imageUrl = fileStorageService.storeFile(image, productId);

            // Ajouter l'image à la liste si elle n'y est pas déjà
            List<String> images = product.getImages();
            if (images == null) {
                images = new ArrayList<>();
            }
            if (!images.contains(imageUrl)) {
                images.add(imageUrl);
            }

            product.setImages(images);
            product.setMainImage(imageUrl);

            productRepository.save(product);

            return ResponseEntity.ok(productService.getProductById(productId));

        } catch (IOException e) {
            log.error("Erreur lors de l'upload de l'image principale", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Définir une image principale (URL ou chemin local)
     */
    @PostMapping("/{productId}/images/main")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> setMainImage(
            @PathVariable Long productId,
            @RequestParam String imageUrl) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + productId));

        // Vérifier que l'image existe dans la liste
        if (product.getImages() == null || !product.getImages().contains(imageUrl)) {
            return ResponseEntity.badRequest().build();
        }

        product.setMainImage(imageUrl);
        productRepository.save(product);

        return ResponseEntity.ok(productService.getProductById(productId));
    }

    /**
     * Supprimer une image
     */
    @DeleteMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UploadResponse> deleteProductImage(
            @PathVariable Long productId,
            @RequestParam String imageUrl) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + productId));

        if (product.getImages() != null && product.getImages().remove(imageUrl)) {

            // Si c'est une image locale (uploadée), supprimer le fichier physique
            if (imageUrl.startsWith("/uploads/")) {
                fileStorageService.deleteFile(imageUrl);
            }

            // Si l'image principale est supprimée, en définir une nouvelle
            if (imageUrl.equals(product.getMainImage())) {
                if (!product.getImages().isEmpty()) {
                    product.setMainImage(product.getImages().get(0));
                } else {
                    product.setMainImage(null);
                }
            }

            productRepository.save(product);

            return ResponseEntity.ok(new UploadResponse(
                    null,
                    "Image supprimée avec succès",
                    true
            ));
        }

        return ResponseEntity.badRequest().body(new UploadResponse(
                null,
                "Image non trouvée",
                false
        ));
    }

    /**
     * Supprimer toutes les images
     */
    @DeleteMapping("/{productId}/images/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UploadResponse> deleteAllProductImages(@PathVariable Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + productId));

        // Supprimer uniquement les fichiers physiques pour les images locales
        if (product.getImages() != null) {
            for (String imageUrl : product.getImages()) {
                if (imageUrl.startsWith("/uploads/")) {
                    fileStorageService.deleteFile(imageUrl);
                }
            }
        }

        // Vider les listes d'images
        product.setImages(new ArrayList<>());
        product.setMainImage(null);
        productRepository.save(product);

        return ResponseEntity.ok(new UploadResponse(
                null,
                "Toutes les images ont été supprimées",
                true
        ));
    }
}