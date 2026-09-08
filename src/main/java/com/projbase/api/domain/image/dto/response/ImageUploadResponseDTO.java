package com.projbase.api.domain.image.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponseDTO {

    private String url;

    public static ImageUploadResponseDTO of(String url) {
        return ImageUploadResponseDTO.builder().url(url).build();
    }
}
