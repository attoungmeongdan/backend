package com.atmd.backend.global.external.ai.client;

import com.atmd.backend.global.external.ai.config.AiConfig;
import com.atmd.backend.global.external.ai.dto.request.ChatRequestDTO;
import com.atmd.backend.global.external.ai.dto.request.EmbeddingRequestDTO;
import com.atmd.backend.global.external.ai.dto.response.ChatResponseDTO;
import com.atmd.backend.global.external.ai.dto.response.EmbeddingResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "openai",
        url = "${ai.openai.base-url}",
        configuration = AiConfig.class
)
public interface AiClient {

    @PostMapping("/v1/chat/completions")
    ChatResponseDTO chat(@RequestBody ChatRequestDTO request);

    @PostMapping("/v1/embeddings")
    EmbeddingResponseDTO embed(@RequestBody EmbeddingRequestDTO request);
}
