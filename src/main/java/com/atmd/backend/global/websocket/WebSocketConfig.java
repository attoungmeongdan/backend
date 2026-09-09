package com.atmd.backend.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final ExercisePoseWebSocketHandler exercisePoseWebSocketHandler;
    private final ExerciseWebSocketHandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(exercisePoseWebSocketHandler, "/ws/v1/exercise-sessions/*")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("http://localhost:3000", "http://localhost:5173");
    }
}
