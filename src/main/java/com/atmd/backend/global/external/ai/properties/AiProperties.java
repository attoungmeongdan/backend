package com.atmd.backend.global.external.ai.properties;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "ai.openai")
public class AiProperties {

    private String apiKey;
    private String baseUrl = "https://api.openai.com";
    private String model = "gpt-4o-mini";
    private int maxTokens = 2048;
    private String embeddingModel = "text-embedding-3-small";
    @Min(1536)
    @Max(1536)
    private int embeddingDimensions = 1536;
    private int embeddingBatchSize = 100;
    private boolean embeddingInitializeOnStartup;
}
