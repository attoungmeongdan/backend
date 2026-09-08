package com.atmd.api.domain.image.service;

import com.atmd.api.domain.image.dto.response.ImageUploadResponseDTO;
import com.atmd.api.domain.image.enums.UploadDirectory;
import com.atmd.api.global.s3.service.S3Service;
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
