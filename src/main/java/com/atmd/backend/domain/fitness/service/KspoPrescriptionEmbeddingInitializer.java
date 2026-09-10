package com.atmd.backend.domain.fitness.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.atmd.backend.global.external.ai.properties.AiProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class KspoPrescriptionEmbeddingInitializer implements ApplicationRunner {

    private final KspoPrescriptionEmbeddingService embeddingService;
    private final AiProperties aiProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!aiProperties.isEmbeddingInitializeOnStartup()) {
            log.info("[KSPO_EMBEDDING] 자동 임베딩이 비활성화되어 있습니다. "
                    + "OPENAI_EMBEDDING_INITIALIZE_ON_STARTUP=false");
            return;
        }

        log.info("[KSPO_EMBEDDING] 서버 시작 임베딩 작업을 실행합니다.");
        try {
            embeddingService.embedPendingPrescriptions();
        } catch (RuntimeException exception) {
            log.error("[KSPO_EMBEDDING] 임베딩 작업에 실패했습니다. 완료된 배치는 유지되며 "
                    + "다음 실행에서 미처리 데이터부터 재개합니다.", exception);
            throw exception;
        }
    }
}
