package com.atmd.backend.global.websocket;

import com.atmd.backend.domain.fitness.service.ExerciseSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExerciseWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    public static final String SESSION_ID_ATTRIBUTE = "exerciseSessionId";

    private final ExerciseSessionService exerciseSessionService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Long sessionId = extractSessionId(request);
        MultiValueMap<String, String> query = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams();
        String ticket = query.getFirst("ticket");

        if (sessionId == null || ticket == null || !exerciseSessionService.reserveWebSocketTicket(sessionId, ticket)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(SESSION_ID_ATTRIBUTE, sessionId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        Long sessionId = extractSessionId(request);
        String ticket = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("ticket");
        if (sessionId != null && ticket != null) {
            exerciseSessionService.finishWebSocketHandshake(sessionId, ticket, exception == null);
        }
    }

    private Long extractSessionId(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        String prefix = "/ws/v1/exercise-sessions/";
        if (!path.startsWith(prefix)) {
            return null;
        }

        String value = path.substring(prefix.length());
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
