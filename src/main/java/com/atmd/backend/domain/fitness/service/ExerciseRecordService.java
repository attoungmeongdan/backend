package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.response.ExerciseMeasurementHistoryResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementHistoryResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementHistoryValueResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasuredExerciseResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementResultsResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.fitness.exception.FitnessErrorCode;
import com.atmd.backend.global.common.exception.GeneralException;
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
    private static final int RECENT_MEASUREMENT_LIMIT = 5;

    private final ExerciseSessionRepository exerciseSessionRepository;

    @Transactional(readOnly = true)
    public MeasurementHistoryResponse getMeasurementHistory(Long userId) {
        List<String> groupIds = exerciseSessionRepository.findRecentCompletedMeasurementGroupIds(
                userId,
                ExerciseSessionMode.MEASUREMENT,
                ExerciseSessionStatus.COMPLETED,
                PageRequest.of(0, RECENT_MEASUREMENT_LIMIT)
        );
        if (groupIds.isEmpty()) {
            return emptyMeasurementHistory();
        }

        Map<String, List<ExerciseSession>> sessionsByGroup = new LinkedHashMap<>();
        groupIds.forEach(groupId -> sessionsByGroup.put(groupId, new ArrayList<>()));
        exerciseSessionRepository.findAllByUserIdAndMeasurementGroupIdInAndStatusAndIsDeletedFalse(
                userId, groupIds, ExerciseSessionStatus.COMPLETED
        ).forEach(session -> sessionsByGroup.get(session.getMeasurementGroupId()).add(session));

        List<HistoryRecord> records = groupIds.stream()
                .map(sessionsByGroup::get)
                .filter(sessions -> sessions.size() == ExerciseType.values().length)
                .map(this::toRecord)
                .sorted(Comparator.comparing(HistoryRecord::measuredAt).reversed())
                .limit(RECENT_MEASUREMENT_LIMIT)
                .toList();

        LocalDate today = LocalDate.now(SERVER_ZONE);
        HistoryRecord todayRecord = records.stream()
                .filter(record -> record.measuredAt().toLocalDate().equals(today))
                .findFirst()
                .orElse(null);
        List<HistoryRecord> previous = records.stream()
                .filter(record -> todayRecord == null || !record.measurementGroupId().equals(todayRecord.measurementGroupId()))
                .limit(todayRecord == null ? RECENT_MEASUREMENT_LIMIT : RECENT_MEASUREMENT_LIMIT - 1)
                .toList();
        return new MeasurementHistoryResponse(
                toExerciseHistory(ExerciseType.CHAIR_STAND, todayRecord, previous),
                toExerciseHistory(ExerciseType.SIT_UP, todayRecord, previous),
                toExerciseHistory(ExerciseType.PUSH_UP, todayRecord, previous),
                toExerciseHistory(ExerciseType.PLANK, todayRecord, previous)
        );
    }

    @Transactional(readOnly = true)
    public MeasurementResultsResponse getMeasurementResults(Long userId, String measurementGroupId) {
        List<ExerciseSession> allSessions = exerciseSessionRepository
                .findAllByUserIdAndMeasurementGroupIdAndIsDeletedFalse(userId, measurementGroupId);
        if (allSessions.isEmpty()) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_RESULT_NOT_FOUND);
        }

        Map<ExerciseType, ExerciseSession> completedByType = new EnumMap<>(ExerciseType.class);
        allSessions.stream()
                .filter(session -> session.getMode() == ExerciseSessionMode.MEASUREMENT)
                .filter(session -> session.getStatus() == ExerciseSessionStatus.COMPLETED)
                .sorted(Comparator.comparing(ExerciseSession::getCompletedAt))
                .forEach(session -> completedByType.put(session.getExerciseType(), session));
        if (completedByType.size() != ExerciseType.values().length) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_INCOMPLETE);
        }

        List<MeasuredExerciseResponse> exercises = List.of(ExerciseType.values()).stream()
                .map(type -> {
                    ExerciseSession session = completedByType.get(type);
                    boolean plank = type == ExerciseType.PLANK;
                    double value = plank ? session.getValidDurationMs() / 1000.0 : session.getValidCount();
                    return new MeasuredExerciseResponse(
                            type,
                            roundOneDecimal(value),
                            plank ? "SECOND" : "COUNT"
                    );
                })
                .toList();
        LocalDateTime measuredAt = completedByType.values().stream()
                .map(ExerciseSession::getCompletedAt)
                .max(LocalDateTime::compareTo)
                .orElseThrow();
        return new MeasurementResultsResponse(measurementGroupId, measuredAt, exercises);
    }

    private HistoryRecord toRecord(List<ExerciseSession> sessions) {
        Map<ExerciseType, Double> values = new EnumMap<>(ExerciseType.class);
        LocalDateTime measuredAt = null;
        for (ExerciseSession session : sessions) {
            boolean plank = session.getExerciseType() == ExerciseType.PLANK;
            double value = plank ? session.getValidDurationMs() / 1000.0 : session.getValidCount();
            values.put(session.getExerciseType(), roundOneDecimal(value));
            if (measuredAt == null || session.getCompletedAt().isAfter(measuredAt)) {
                measuredAt = session.getCompletedAt();
            }
        }
        return new HistoryRecord(
                sessions.get(0).getMeasurementGroupId(),
                measuredAt,
                Map.copyOf(values)
        );
    }

    private ExerciseMeasurementHistoryResponse toExerciseHistory(
            ExerciseType exerciseType,
            HistoryRecord today,
            List<HistoryRecord> previous
    ) {
        MeasurementHistoryValueResponse todayValue = today == null ? null : toHistoryValue(today, exerciseType);
        List<MeasurementHistoryValueResponse> previousValues = previous.stream()
                .map(record -> toHistoryValue(record, exerciseType))
                .toList();
        return new ExerciseMeasurementHistoryResponse(todayValue, previousValues);
    }

    private MeasurementHistoryValueResponse toHistoryValue(HistoryRecord record, ExerciseType exerciseType) {
        return new MeasurementHistoryValueResponse(
                record.measurementGroupId(),
                record.measuredAt(),
                record.values().get(exerciseType),
                exerciseType == ExerciseType.PLANK ? "SECOND" : "COUNT"
        );
    }

    private MeasurementHistoryResponse emptyMeasurementHistory() {
        ExerciseMeasurementHistoryResponse empty = new ExerciseMeasurementHistoryResponse(null, List.of());
        return new MeasurementHistoryResponse(empty, empty, empty, empty);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record HistoryRecord(
            String measurementGroupId,
            LocalDateTime measuredAt,
            Map<ExerciseType, Double> values
    ) {
    }
}
