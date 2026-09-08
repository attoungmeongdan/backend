package com.projbase.api.global.external.ai.service;

import com.projbase.api.global.external.ai.client.AiClient;
import com.projbase.api.global.external.ai.dto.request.ChatMessageDTO;
import com.projbase.api.global.external.ai.dto.request.ChatRequestDTO;
import com.projbase.api.global.external.ai.dto.response.ChatResponseDTO;
import com.projbase.api.global.external.ai.properties.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiClient aiClient;
    private final AiProperties aiProperties;

    public String chat(String systemPrompt, String userMessage) {
        ChatRequestDTO request = ChatRequestDTO.builder()
                .model(aiProperties.getModel())
                .maxTokens(aiProperties.getMaxTokens())
                .messages(List.of(
                        ChatMessageDTO.builder().role("system").content(systemPrompt).build(),
                        ChatMessageDTO.builder().role("user").content(userMessage).build()
                ))
                .build();

        ChatResponseDTO response = aiClient.chat(request);
        return response.getChoices().get(0).getMessage().getContent();
    }

    public String chat(String userMessage) {
        return chat("You are a helpful assistant.", userMessage);
    }
}
