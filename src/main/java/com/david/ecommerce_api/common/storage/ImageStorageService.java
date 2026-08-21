package com.david.ecommerce_api.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    UploadedImage upload(MultipartFile file, String folder);
    
    void delete(String publicId);
}
