package com.atmd.backend.global.s3.service;

import com.atmd.backend.domain.image.exception.ImageErrorCode;
import com.atmd.backend.global.common.exception.GeneralException;
import com.atmd.backend.global.s3.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final Map<String, String> EXT_TO_MIME = Map.of(
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".gif", "image/gif",
            ".webp", "image/webp"
    );

    public String upload(MultipartFile file, String directory) {
        validateFile(file);
        String ext = extractExtension(file.getOriginalFilename());
        String resolvedContentType = EXT_TO_MIME.getOrDefault(ext, "application/octet-stream");
        String key = directory + "/" + UUID.randomUUID() + ext;
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(awsProperties.getS3().getBucket())
                            .key(key)
                            .contentType(resolvedContentType)
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", key, e);
            throw new GeneralException(ImageErrorCode.UPLOAD_FAILED);
        }
        return getCloudFrontUrl(key);
    }

    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(awsProperties.getS3().getBucket())
                        .key(key)
                        .build()
        );
    }

    public String getCloudFrontUrl(String key) {
        return "https://" + awsProperties.getS3().getCloudfrontDomain() + "/" + key;
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new GeneralException(ImageErrorCode.INVALID_FILE_TYPE);
        }
        String ext = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new GeneralException(ImageErrorCode.INVALID_FILE_TYPE);
        }
        validateMagicBytes(file);
    }

    private void validateMagicBytes(MultipartFile file) {
        byte[] header = new byte[12];
        try (InputStream is = file.getInputStream()) {
            if (is.read(header) < 12) throw new GeneralException(ImageErrorCode.INVALID_FILE_TYPE);
        } catch (IOException e) {
            throw new GeneralException(ImageErrorCode.INVALID_FILE_TYPE);
        }
        if (!isKnownImageSignature(header)) {
            throw new GeneralException(ImageErrorCode.INVALID_FILE_TYPE);
        }
    }

    private boolean isKnownImageSignature(byte[] b) {
        // JPEG: FF D8 FF
        if (b[0] == (byte) 0xFF && b[1] == (byte) 0xD8 && b[2] == (byte) 0xFF) return true;
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (b[0] == (byte) 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47) return true;
        // GIF: GIF87a / GIF89a
        if (b[0] == 0x47 && b[1] == 0x49 && b[2] == 0x46 && b[3] == 0x38) return true;
        // WebP: RIFF????WEBP
        if (b[0] == 0x52 && b[1] == 0x49 && b[2] == 0x46 && b[3] == 0x46
                && b[8] == 0x57 && b[9] == 0x45 && b[10] == 0x42 && b[11] == 0x50) return true;
        return false;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext) ? ext : "";
    }
}
