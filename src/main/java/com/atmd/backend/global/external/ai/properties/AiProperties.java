package com.atmd.backend.global.external.ai.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.openai")
public class AiProperties {

    private String apiKey;
    private String baseUrl = "https://api.openai.com";
    private String model = "gpt-4o-mini";
    private int maxTokens = 2048;
}
