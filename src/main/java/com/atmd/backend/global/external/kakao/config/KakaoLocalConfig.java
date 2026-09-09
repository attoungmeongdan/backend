package com.atmd.backend.global.external.kakao.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class KakaoLocalConfig {

    @Value("${kakao.local.rest-api-key:}")
    private String restApiKey;

    @Bean
    public RequestInterceptor kakaoAuthInterceptor() {
        return template -> template.header("Authorization", "KakaoAK " + restApiKey);
    }
}
