package com.atmd.api.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS는 SecurityConfig에서 관리
    // 추가 MVC 설정 필요 시 여기에 작성
}
