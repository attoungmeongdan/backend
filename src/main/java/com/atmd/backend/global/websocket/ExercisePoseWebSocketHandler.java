package com.atmd.backend.global.websocket;

import com.atmd.backend.domain.fitness.dto.request.PoseFrameMessage;
import com.atmd.backend.domain.fitness.dto.response.FrameAnalysisResponse;
import com.atmd.backend.domain.fitness.service.ExerciseSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExercisePoseWebSocketHandler extends TextWebSocketHandler {
    private static final int MAX_MESSAGE_SIZE_BYTES = 64 * 1024;

    private final ObjectMapper objectMapper;
    private final ExerciseSessionService exerciseSessionService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(MAX_MESSAGE_SIZE_BYTES);
    }

    @Override
    protected void handleTextMessage(WebSocketSession socketSession, TextMessage message) throws IOException {
        Long exerciseSessionId = (Long) socketSession.getAttributes()
                .get(ExerciseWebSocketHandshakeInterceptor.SESSION_ID_ATTRIBUTE);
        if (exerciseSessionId == null) {
            socketSession.close(CloseStatus.POLICY_VIOLATION.withReason("Missing exercise session"));
            return;
        }

        try {
            PoseFrameMessage frame = objectMapper.readValue(message.getPayload(), PoseFrameMessage.class);
            FrameAnalysisResponse response = exerciseSessionService.processFrame(exerciseSessionId, frame);
            socketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            if ("SESSION_COMPLETED".equals(response.type())) {
                socketSession.close(CloseStatus.NORMAL.withReason("Exercise session completed"));
            }
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            sendError(socketSession, "INVALID_POSE_FRAME", exception.getMessage());
        } catch (Exception exception) {
            log.warn("Exercise WebSocket processing failed for session {}", exerciseSessionId, exception);
            sendError(socketSession, "EXERCISE_PROCESSING_FAILED", "관절 프레임을 처리하지 못했습니다.");
        }
    }

    private void sendError(WebSocketSession session, String code, String message) throws IOException {
        Map<String, String> body = Map.of(
                "type", "ERROR",
                "code", code,
                "message", message == null ? "요청을 처리하지 못했습니다." : message
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(body)));
    }
}
