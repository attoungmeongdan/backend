package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.response.MeasurementExerciseValueResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementHistoryResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementRecordResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExerciseRecordService {
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Seoul");
    private static final int PREVIOUS_MEASUREMENT_LIMIT = 5;

    private final ExerciseSessionRepository exerciseSessionRepository;

    @Transactional(readOnly = true)
    public MeasurementHistoryResponse getMeasurementHistory(Long userId) {
        List<String> groupIds = exerciseSessionRepository.findRecentCompletedMeasurementGroupIds(
                userId,
                ExerciseSessionMode.MEASUREMENT,
                ExerciseSessionStatus.COMPLETED,
                PageRequest.of(0, PREVIOUS_MEASUREMENT_LIMIT + 1)
        );
        if (groupIds.isEmpty()) {
            return new MeasurementHistoryResponse(null, List.of());
        }

        Map<String, List<ExerciseSession>> sessionsByGroup = new LinkedHashMap<>();
        groupIds.forEach(groupId -> sessionsByGroup.put(groupId, new ArrayList<>()));
        exerciseSessionRepository.findAllByUserIdAndMeasurementGroupIdInAndStatusAndIsDeletedFalse(
                userId, groupIds, ExerciseSessionStatus.COMPLETED
        ).forEach(session -> sessionsByGroup.get(session.getMeasurementGroupId()).add(session));

        List<MeasurementRecordResponse> records = groupIds.stream()
                .map(sessionsByGroup::get)
                .filter(sessions -> sessions.size() == ExerciseType.values().length)
                .map(this::toRecord)
                .sorted(Comparator.comparing(MeasurementRecordResponse::measuredAt).reversed())
                .toList();

        LocalDate today = LocalDate.now(SERVER_ZONE);
        MeasurementRecordResponse todayRecord = records.stream()
                .filter(record -> record.measuredAt().toLocalDate().equals(today))
                .findFirst()
                .orElse(null);
        List<MeasurementRecordResponse> previous = records.stream()
                .filter(record -> todayRecord == null || !record.measurementGroupId().equals(todayRecord.measurementGroupId()))
                .limit(PREVIOUS_MEASUREMENT_LIMIT)
                .toList();
        return new MeasurementHistoryResponse(todayRecord, previous);
    }

    private MeasurementRecordResponse toRecord(List<ExerciseSession> sessions) {
        Map<ExerciseType, MeasurementExerciseValueResponse> exercises = new EnumMap<>(ExerciseType.class);
        double totalScore = 0;
        LocalDateTime measuredAt = null;
        for (ExerciseSession session : sessions) {
            boolean plank = session.getExerciseType() == ExerciseType.PLANK;
            double value = plank ? session.getValidDurationMs() / 1000.0 : session.getValidCount();
            value = roundOneDecimal(value);
            exercises.put(
                    session.getExerciseType(),
                    new MeasurementExerciseValueResponse(value, plank ? "SECOND" : "COUNT")
            );
            totalScore += value;
            if (measuredAt == null || session.getCompletedAt().isAfter(measuredAt)) {
                measuredAt = session.getCompletedAt();
            }
        }
        return new MeasurementRecordResponse(
                sessions.get(0).getMeasurementGroupId(),
                measuredAt,
                roundOneDecimal(totalScore),
                Map.copyOf(exercises)
        );
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
