package com.atmd.backend.global.external.kakao.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakao.local")
public class KakaoLocalProperties {

    private String restApiKey;
    private String baseUrl = "https://dapi.kakao.com";
}
