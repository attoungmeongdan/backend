package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.global.external.ai.client.AiClient;
import com.atmd.backend.global.external.ai.dto.request.EmbeddingRequestDTO;
import com.atmd.backend.global.external.ai.dto.response.EmbeddingDataDTO;
import com.atmd.backend.global.external.ai.dto.response.EmbeddingResponseDTO;
import com.atmd.backend.global.external.ai.properties.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class KspoPrescriptionEmbeddingService {

    private final JdbcTemplate jdbcTemplate;
    private final AiClient aiClient;
    private final AiProperties aiProperties;

    public int embedPendingPrescriptions() {
        validateConfiguration();

        int initialPendingCount = countPendingPrescriptions();
        log.info("[KSPO_EMBEDDING] 임베딩 작업을 시작합니다. model={}, dimensions={}, "
                        + "batchSize={}, pending={}",
                aiProperties.getEmbeddingModel(),
                aiProperties.getEmbeddingDimensions(),
                aiProperties.getEmbeddingBatchSize(),
                initialPendingCount);

        int totalEmbedded = 0;
        while (true) {
            List<PendingPrescription> pending = findPendingBatch();
            if (pending.isEmpty()) {
                log.info("[KSPO_EMBEDDING] 임베딩 작업을 완료했습니다. requested={}, embedded={}",
                        initialPendingCount, totalEmbedded);
                return totalEmbedded;
            }

            int rangeStart = totalEmbedded + 1;
            int rangeEnd = totalEmbedded + pending.size();
            log.info("[KSPO_EMBEDDING] {}~{} 임베딩 요청을 시작합니다.", rangeStart, rangeEnd);
            EmbeddingResponseDTO response = aiClient.embed(EmbeddingRequestDTO.builder()
                    .model(aiProperties.getEmbeddingModel())
                    .dimensions(aiProperties.getEmbeddingDimensions())
                    .input(pending.stream().map(PendingPrescription::content).toList())
                    .build());

            List<EmbeddingDataDTO> embeddings = validateAndSortResponse(response, pending.size());
            updateEmbeddings(pending, embeddings);
            totalEmbedded += pending.size();
            log.info("[KSPO_EMBEDDING] {}~{} 적재를 완료했습니다. total={}, remaining={}",
                    rangeStart, rangeEnd, totalEmbedded,
                    Math.max(0, initialPendingCount - totalEmbedded));
        }
    }

    private int countPendingPrescriptions() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM kspo_exercise_prescription
                WHERE embedding IS NULL
                """, Integer.class);
        return count == null ? 0 : count;
    }

    private List<PendingPrescription> findPendingBatch() {
        return jdbcTemplate.query("""
                        SELECT id, prescription_content
                        FROM kspo_exercise_prescription
                        WHERE embedding IS NULL
                        ORDER BY id
                        LIMIT ?
                        """,
                (resultSet, rowNumber) -> new PendingPrescription(
                        resultSet.getLong("id"),
                        resultSet.getString("prescription_content")
                ),
                aiProperties.getEmbeddingBatchSize());
    }

    private List<EmbeddingDataDTO> validateAndSortResponse(
            EmbeddingResponseDTO response,
            int expectedSize
    ) {
        if (response == null || response.getData() == null
                || response.getData().size() != expectedSize) {
            throw new IllegalStateException("OpenAI 임베딩 응답 개수가 요청 개수와 일치하지 않습니다.");
        }

        List<EmbeddingDataDTO> embeddings = response.getData().stream()
                .sorted(Comparator.comparingInt(EmbeddingDataDTO::getIndex))
                .toList();
        for (int index = 0; index < embeddings.size(); index++) {
            EmbeddingDataDTO embedding = embeddings.get(index);
            if (embedding.getIndex() != index || embedding.getEmbedding() == null
                    || embedding.getEmbedding().size() != aiProperties.getEmbeddingDimensions()) {
                throw new IllegalStateException("OpenAI 임베딩 응답 형식이 올바르지 않습니다.");
            }
        }
        return embeddings;
    }

    private void updateEmbeddings(
            List<PendingPrescription> pending,
            List<EmbeddingDataDTO> embeddings
    ) {
        jdbcTemplate.batchUpdate("""
                        UPDATE kspo_exercise_prescription
                        SET embedding = CAST(? AS vector),
                            embedding_model = ?,
                            embedded_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                          AND embedding IS NULL
                        """,
                pending,
                pending.size(),
                (statement, prescription) -> {
                    int index = pending.indexOf(prescription);
                    statement.setString(1, toVectorLiteral(embeddings.get(index).getEmbedding()));
                    statement.setString(2, aiProperties.getEmbeddingModel());
                    statement.setLong(3, prescription.id());
                });
    }

    private String toVectorLiteral(List<Double> values) {
        return values.stream()
                .map(value -> String.format(Locale.ROOT, "%.10f", value))
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

    private void validateConfiguration() {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY가 설정되지 않았습니다.");
        }
        if (aiProperties.getEmbeddingBatchSize() <= 0) {
            throw new IllegalStateException("임베딩 배치 크기는 1 이상이어야 합니다.");
        }
    }

    private record PendingPrescription(Long id, String content) {
    }
}
