package com.david.ecommerce_api.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.storage.ImageStorageService;
import com.david.ecommerce_api.common.storage.UploadedImage;
import com.david.ecommerce_api.config.CloudinaryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImageStorageService implements ImageStorageService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public UploadedImage upload(MultipartFile file, String folder) {
        validateImage(file);

        try {
            String fullFolder = properties.rootFolder() + "/" + folder;

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", fullFolder,
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false
                    )
            );

            return new UploadedImage(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"),
                    toInteger(result.get("width")),
                    toInteger(result.get("height"))
            );
        } catch (Exception exception) {
            throw new DomainException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );
        } catch (Exception exception) {
            throw new DomainException(ErrorCode.IMAGE_DELETE_FAILED);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainException(ErrorCode.IMAGE_INVALID_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new DomainException(ErrorCode.IMAGE_INVALID_FILE);
        }
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }
}