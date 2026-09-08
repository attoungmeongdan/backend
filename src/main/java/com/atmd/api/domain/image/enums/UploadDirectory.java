package com.atmd.api.domain.image.enums;

import com.atmd.api.domain.image.exception.ImageErrorCode;
import com.atmd.api.global.common.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UploadDirectory {

    PROFILES("profiles"),
    ATTACHMENTS("attachments");

    private final String path;

    public static UploadDirectory from(String value) {
        for (UploadDirectory dir : values()) {
            if (dir.path.equalsIgnoreCase(value)) return dir;
        }
        throw new GeneralException(ImageErrorCode.INVALID_DIRECTORY);
    }
}
