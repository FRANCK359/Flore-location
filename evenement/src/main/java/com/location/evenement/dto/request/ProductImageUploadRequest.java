package com.location.evenement.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class ProductImageUploadRequest {
    private List<MultipartFile> images;
    private Long productId;
    private Boolean setAsMain; // Pour définir la première image comme principale
}