package com.atmd.backend.domain.image.controller;

import com.atmd.backend.domain.image.dto.response.ImageUploadResponseDTO;
import com.atmd.backend.domain.image.service.ImageService;
import com.atmd.backend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // directory: profiles, attachments 등 용도별 구분
    @PostMapping(value = "/{directory}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponseDTO>> upload(
            @PathVariable String directory,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(imageService.upload(file, directory)));
    }
}
