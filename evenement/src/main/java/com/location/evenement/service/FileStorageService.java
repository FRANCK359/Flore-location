package com.location.evenement.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface FileStorageService {
    String storeFile(MultipartFile file, Long productId) throws IOException;
    List<String> storeMultipleFiles(List<MultipartFile> files, Long productId) throws IOException;
    void deleteFile(String fileName);
    void deleteProductFiles(Long productId);
    String getFileUrl(String fileName);
}