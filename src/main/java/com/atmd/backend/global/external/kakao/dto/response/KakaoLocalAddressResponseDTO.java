package com.atmd.backend.global.external.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class KakaoLocalAddressResponseDTO {

    private List<Document> documents;

    @Getter
    public static class Document {
        private String x; // 경도(lng)
        private String y; // 위도(lat)
    }

    public Double getLat() {
        if (documents == null || documents.isEmpty()) return null;
        String y = documents.get(0).getY();
        return y != null ? Double.parseDouble(y) : null;
    }

    public Double getLng() {
        if (documents == null || documents.isEmpty()) return null;
        String x = documents.get(0).getX();
        return x != null ? Double.parseDouble(x) : null;
    }
}
