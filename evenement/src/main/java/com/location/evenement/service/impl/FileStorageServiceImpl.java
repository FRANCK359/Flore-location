package com.location.evenement.service.impl;

import com.location.evenement.exception.BadRequestException;
import com.location.evenement.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.allowed-extensions:jpg,jpeg,png,gif}")
    private String allowedExtensions;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    private Path fileStoragePath;
    private final List<String> allowedExtList = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Utiliser le chemin absolu
        this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStoragePath);
            log.info("Dossier de stockage créé: {}", this.fileStoragePath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier de stockage: " + this.fileStoragePath, e);
        }

        String[] extensions = allowedExtensions.split(",");
        for (String ext : extensions) {
            allowedExtList.add(ext.trim().toLowerCase());
        }
    }

    @Override
    public String storeFile(MultipartFile file, Long productId) throws IOException {
        validateFile(file);

        // Créer le dossier spécifique au produit
        Path productPath = this.fileStoragePath.resolve("product_" + productId);
        Files.createDirectories(productPath);

        log.info("Sauvegarde dans le dossier: {}", productPath.toAbsolutePath());

        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String newFileName = UUID.randomUUID().toString() + "." + fileExtension;

        Path targetPath = productPath.resolve(newFileName);

        // Copier le fichier
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Fichier sauvegardé: {}", targetPath.toAbsolutePath());

        // Retourner l'URL complète pour accéder à l'image
        return buildFileUrl(productId, newFileName);
    }

    @Override
    public List<String> storeMultipleFiles(List<MultipartFile> files, Long productId) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = storeFile(file, productId);
                    fileUrls.add(fileUrl);
                }
            }
        }
        return fileUrls;
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extraire le chemin relatif de l'URL
            String relativePath = extractPathFromUrl(fileUrl);
            Path filePath = Paths.get(relativePath);

            if (!filePath.isAbsolute()) {
                filePath = this.fileStoragePath.resolve(filePath);
            }

            Files.deleteIfExists(filePath);
            log.info("Fichier supprimé: {}", filePath);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier: {}", fileUrl, e);
        }
    }

    @Override
    public void deleteProductFiles(Long productId) {
        try {
            Path productPath = this.fileStoragePath.resolve("product_" + productId);
            if (Files.exists(productPath)) {
                Files.walk(productPath)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                                log.info("Supprimé: {}", path);
                            } catch (IOException e) {
                                log.error("Erreur lors de la suppression: {}", path, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Erreur lors de la suppression des fichiers du produit {}", productId, e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        return fileName; // Retourne l'URL complète déjà stockée
    }

    private String buildFileUrl(Long productId, String fileName) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        if (baseUrl.isEmpty()) {
            baseUrl = "http://localhost:" + serverPort + contextPath;
        }
        return baseUrl + "/uploads/product_" + productId + "/" + fileName;
    }

    private String extractPathFromUrl(String fileUrl) {
        // Extraire la partie après /uploads/
        int index = fileUrl.indexOf("/uploads/");
        if (index != -1) {
            return fileUrl.substring(index + 9); // +9 pour enlever "/uploads/"
        }
        return fileUrl;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Fichier vide");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BadRequestException("Nom de fichier invalide");
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (!allowedExtList.contains(fileExtension)) {
            throw new BadRequestException("Extension non autorisée: " + fileExtension);
        }

        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Le fichier n'est pas une image: " + contentType);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new BadRequestException("Fichier sans extension");
        }
        return fileName.substring(lastDotIndex + 1);
    }
}