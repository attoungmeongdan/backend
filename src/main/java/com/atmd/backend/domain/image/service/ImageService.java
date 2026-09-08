package com.atmd.backend.domain.image.service;

import com.atmd.backend.domain.image.dto.response.ImageUploadResponseDTO;
import com.atmd.backend.domain.image.enums.UploadDirectory;
import com.atmd.backend.global.s3.service.S3Service;
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
