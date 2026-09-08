package com.projbase.api.domain.image.service;

import com.projbase.api.domain.image.dto.response.ImageUploadResponseDTO;
import com.projbase.api.domain.image.enums.UploadDirectory;
import com.projbase.api.global.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Service s3Service;

    public ImageUploadResponseDTO upload(MultipartFile file, String directory) {
        UploadDirectory uploadDir = UploadDirectory.from(directory);
        String url = s3Service.upload(file, uploadDir.getPath());
        return ImageUploadResponseDTO.of(url);
    }
}
