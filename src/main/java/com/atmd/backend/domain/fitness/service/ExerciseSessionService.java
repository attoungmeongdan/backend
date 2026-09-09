package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.analysis.ChairStandAnalyzer;
import com.atmd.backend.domain.fitness.analysis.ChairStandMetrics;
import com.atmd.backend.domain.fitness.analysis.PushUpAnalyzer;
import com.atmd.backend.domain.fitness.analysis.PushUpMetrics;
import com.atmd.backend.domain.fitness.analysis.SitUpAnalyzer;
import com.atmd.backend.domain.fitness.analysis.SitUpMetrics;
import com.atmd.backend.domain.fitness.analysis.PlankAnalyzer;
import com.atmd.backend.domain.fitness.analysis.PlankMetrics;
import com.atmd.backend.domain.fitness.enums.PlankPhase;
import com.atmd.backend.domain.fitness.dto.request.ExerciseSessionCreateRequest;
import com.atmd.backend.domain.fitness.dto.request.PoseFrameMessage;
import com.atmd.backend.domain.fitness.dto.response.ExerciseSessionCreateResponse;
import com.atmd.backend.domain.fitness.dto.response.ExerciseSessionResultResponse;
import com.atmd.backend.domain.fitness.dto.response.FrameAnalysisResponse;
import com.atmd.backend.domain.fitness.dto.response.PostureFeedback;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.exception.FitnessErrorCode;
import com.atmd.backend.domain.fitness.pose.PoseFrameSmoother;
import com.atmd.backend.domain.fitness.pose.PoseFrameValidator;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ExerciseSessionService {
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Seoul");
    private static final long SOCKET_TICKET_VALID_SECONDS = 60;

    private final ExerciseSessionRepository exerciseSessionRepository;
    private final UserRepository userRepository;
    private final PoseFrameValidator frameValidator;
    private final PoseFrameSmoother frameSmoother;
    private final ChairStandAnalyzer chairStandAnalyzer;
    private final PushUpAnalyzer pushUpAnalyzer;
    private final SitUpAnalyzer sitUpAnalyzer;
    private final PlankAnalyzer plankAnalyzer;

    @Value("${fitness.exercise-session.idle-timeout-seconds:15}")
    private long idleTimeoutSeconds;

    private final Map<Long, RuntimeExerciseSession> runtimeSessions = new ConcurrentHashMap<>();

    @Transactional
    public ExerciseSessionCreateResponse create(Long userId, ExerciseSessionCreateRequest request) {
        if (request.exerciseType() != ExerciseType.CHAIR_STAND
                && request.exerciseType() != ExerciseType.PUSH_UP
                && request.exerciseType() != ExerciseType.SIT_UP
                && request.exerciseType() != ExerciseType.PLANK) {
            throw new GeneralException(FitnessErrorCode.UNSUPPORTED_EXERCISE);
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new GeneralException(FitnessErrorCode.SESSION_ACCESS_DENIED));

        List<ExerciseSession> activeSessions = exerciseSessionRepository.findAllByUserIdAndStatusIn(
                userId,
                List.of(ExerciseSessionStatus.CREATED, ExerciseSessionStatus.MEASURING)
        );
        Instant now = Instant.now();
        for (ExerciseSession activeSession : activeSessions) {
            RuntimeExerciseSession runtime = runtimeSessions.get(activeSession.getId());
            if (runtime != null && !runtime.isExpired(now)) {
                throw new GeneralException(FitnessErrorCode.ACTIVE_SESSION_EXISTS);
            }
            if (runtime != null) {
                synchronized (runtime) {
                    ExerciseSessionStatus status = runtime.hasTimeLimitElapsed(now)
                            ? ExerciseSessionStatus.COMPLETED
                            : ExerciseSessionStatus.EXPIRED;
                    completeRuntime(runtime, status, now);
                }
            } else {
                activeSession.expire(
                        activeSession.getValidCount(),
                        activeSession.getInvalidCount(),
                        activeSession.getValidDurationMs(),
                        toLocalDateTime(now)
                );
            }
        }

        String measurementGroupId = resolveMeasurementGroupId(userId, request);
        ExerciseSession session = exerciseSessionRepository.save(ExerciseSession.create(
                user,
                request.mode(),
                request.exerciseType(),
                measurementGroupId
        ));

        String ticket = UUID.randomUUID().toString();
        runtimeSessions.put(
                session.getId(),
                new RuntimeExerciseSession(
                        session.getId(),
                        userId,
                        session.getExerciseType(),
                        ticket,
                        Instant.now().plusSeconds(SOCKET_TICKET_VALID_SECONDS),
                        session.getTimeLimitSeconds(),
                        idleTimeoutSeconds
                )
        );
        return ExerciseSessionCreateResponse.from(session, ticket);
    }

    public boolean reserveWebSocketTicket(Long sessionId, String ticket) {
        RuntimeExerciseSession runtime = runtimeSessions.get(sessionId);
        if (runtime == null) {
            return false;
        }
        synchronized (runtime) {
            return runtime.reserveTicket(ticket, Instant.now());
        }
    }

    public void finishWebSocketHandshake(Long sessionId, String ticket, boolean successful) {
        RuntimeExerciseSession runtime = runtimeSessions.get(sessionId);
        if (runtime == null) {
            return;
        }
        synchronized (runtime) {
            if (successful) {
                runtime.confirmTicket(ticket);
            } else {
                runtime.releaseTicket(ticket);
            }
        }
    }

    @Transactional
    public FrameAnalysisResponse processFrame(Long sessionId, PoseFrameMessage frame) {
        RuntimeExerciseSession runtime = getRuntimeSession(sessionId);
        synchronized (runtime) {
            ensureActive(runtime);

            PoseFrameValidator.ValidationResult validation = frameValidator.validate(
                    frame,
                    sessionId,
                    runtime.getExerciseType(),
                    runtime.getLastSequence(),
                    runtime.getLastFrameTimestamp()
            );
            if (!validation.valid()) {
                if (validation.positionRequired()) {
                    return response(
                            "ANALYSIS_RESULT",
                            runtime,
                            frame.sequence(),
                            Map.of(),
                            List.of(PostureFeedback.positioningRequired(runtime.getExerciseType().name()))
                    );
                }
                throw new IllegalArgumentException(validation.code() + ": " + validation.message());
            }

            Instant now = Instant.now();
            runtime.acceptFrame(frame.sequence(), frame.timestamp(), now);
            if (runtime.getStartedAt() == null) {
                runtime.start(now);
                ExerciseSession session = getSession(sessionId);
                session.start(toLocalDateTime(now));
            }

            if (runtime.remainingTimeMs(now) == 0) {
                completeRuntime(runtime, ExerciseSessionStatus.COMPLETED, now);
                return response("SESSION_COMPLETED", runtime, frame.sequence(), Map.of(), List.of());
            }

            List<com.atmd.backend.domain.fitness.dto.request.LandmarkDto> smoothed =
                    frameSmoother.addAndSmooth(runtime.getRecentFrames(), frame.landmarks());
            if (!frameValidator.hasRequiredVisibility(smoothed, runtime.getExerciseType())) {
                return response(
                        "ANALYSIS_RESULT",
                        runtime,
                        frame.sequence(),
                        Map.of(),
                        List.of(PostureFeedback.positioningRequired(runtime.getExerciseType().name()))
                );
            }
            Map<String, Double> metrics = analyze(runtime, smoothed);

            if (runtime.isCompletionPending() || runtime.isCompleted()) {
                return response(
                        "SESSION_COMPLETED",
                        runtime,
                        frame.sequence(),
                        metrics,
                        List.of(PostureFeedback.plankPostureBroken())
                );
            }

            now = Instant.now();
            String type = "ANALYSIS_RESULT";
            if (runtime.remainingTimeMs(now) == 0) {
                completeRuntime(runtime, ExerciseSessionStatus.COMPLETED, now);
                type = "SESSION_COMPLETED";
            }
            return response(type, runtime, frame.sequence(), metrics, List.of());
        }
    }

    @Transactional
    public ExerciseSessionResultResponse complete(Long userId, Long sessionId) {
        ExerciseSession session = getOwnedSession(userId, sessionId);
        if (session.getMode() != ExerciseSessionMode.WORKOUT) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_MANUAL_COMPLETE_NOT_ALLOWED);
        }
        RuntimeExerciseSession runtime = runtimeSessions.get(sessionId);
        if (runtime == null) {
            if (session.getStatus() == ExerciseSessionStatus.COMPLETED) {
                return ExerciseSessionResultResponse.from(session);
            }
            throw new GeneralException(FitnessErrorCode.SESSION_NOT_ACTIVE);
        }

        synchronized (runtime) {
            if (!runtime.isCompleted()) {
                if (runtime.getExerciseType() == ExerciseType.PLANK) {
                    runtime.stopPlankHolding(Instant.now());
                }
                completeRuntime(runtime, ExerciseSessionStatus.COMPLETED, Instant.now());
            }
        }
        return ExerciseSessionResultResponse.from(session);
    }

    @Transactional(readOnly = true)
    public ExerciseSessionResultResponse getResult(Long userId, Long sessionId) {
        return ExerciseSessionResultResponse.from(getOwnedSession(userId, sessionId));
    }

    @Transactional
    public void expireDisconnectedSession(Long sessionId) {
        RuntimeExerciseSession runtime = runtimeSessions.get(sessionId);
        if (runtime == null) {
            return;
        }
        synchronized (runtime) {
            if (!runtime.isCompleted() && !runtime.isCompletionPending()) {
                Instant now = Instant.now();
                if (runtime.getExerciseType() == ExerciseType.PLANK) {
                    runtime.stopPlankHolding(now);
                }
                completeRuntime(runtime, ExerciseSessionStatus.EXPIRED, now);
            }
        }
    }

    @Scheduled(fixedDelayString = "${fitness.exercise-session.cleanup-interval-ms:5000}")
    @Transactional
    public void cleanupExpiredSessions() {
        Instant now = Instant.now();
        for (RuntimeExerciseSession runtime : runtimeSessions.values()) {
            synchronized (runtime) {
                if (!runtime.isCompleted() && !runtime.isCompletionPending() && runtime.isExpired(now)) {
                    if (runtime.getExerciseType() == ExerciseType.PLANK) {
                        runtime.stopPlankHolding(now);
                    }
                    ExerciseSessionStatus status = runtime.hasTimeLimitElapsed(now)
                            ? ExerciseSessionStatus.COMPLETED
                            : ExerciseSessionStatus.EXPIRED;
                    completeRuntime(runtime, status, now);
                }
            }
        }
    }

    private FrameAnalysisResponse response(
            String type,
            RuntimeExerciseSession runtime,
            long sequence,
            Map<String, Double> metrics,
            List<PostureFeedback> feedback
    ) {
        return new FrameAnalysisResponse(
                type,
                runtime.getSessionId(),
                sequence,
                runtime.currentPhase(),
                runtime.validCount(),
                runtime.invalidCount(),
                runtime.remainingTimeMs(Instant.now()),
                runtime.validDurationMs(Instant.now()),
                metrics,
                feedback
        );
    }

    private void completeRuntime(
            RuntimeExerciseSession runtime,
            ExerciseSessionStatus status,
            Instant completedAt
    ) {
        ExerciseSession session = getSession(runtime.getSessionId());
        if (status == ExerciseSessionStatus.EXPIRED) {
            session.expire(
                    runtime.validCount(),
                    runtime.invalidCount(),
                    runtime.validDurationMs(completedAt),
                    toLocalDateTime(completedAt)
            );
        } else {
            session.complete(
                    runtime.validCount(),
                    runtime.invalidCount(),
                    runtime.validDurationMs(completedAt),
                    toLocalDateTime(completedAt)
            );
        }
        finalizeRuntimeAfterCommit(runtime);
    }

    private void finalizeRuntimeAfterCommit(RuntimeExerciseSession runtime) {
        runtime.scheduleCompletion();
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runtime.complete();
            runtimeSessions.remove(runtime.getSessionId(), runtime);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                synchronized (runtime) {
                    runtime.complete();
                    runtimeSessions.remove(runtime.getSessionId(), runtime);
                }
            }

            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    synchronized (runtime) {
                        runtime.cancelCompletion();
                    }
                }
            }
        });
    }

    private ExerciseSession getOwnedSession(Long userId, Long sessionId) {
        ExerciseSession session = getSession(sessionId);
        if (!session.belongsTo(userId)) {
            throw new GeneralException(FitnessErrorCode.SESSION_ACCESS_DENIED);
        }
        return session;
    }

    private ExerciseSession getSession(Long sessionId) {
        return exerciseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GeneralException(FitnessErrorCode.SESSION_NOT_FOUND));
    }

    private RuntimeExerciseSession getRuntimeSession(Long sessionId) {
        RuntimeExerciseSession runtime = runtimeSessions.get(sessionId);
        if (runtime == null) {
            throw new GeneralException(FitnessErrorCode.SESSION_NOT_ACTIVE);
        }
        return runtime;
    }

    private void ensureActive(RuntimeExerciseSession runtime) {
        if (runtime.isCompleted() || runtime.isCompletionPending()) {
            throw new GeneralException(FitnessErrorCode.SESSION_NOT_ACTIVE);
        }
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, SERVER_ZONE);
    }

    private String resolveMeasurementGroupId(Long userId, ExerciseSessionCreateRequest request) {
        if (request.mode() == ExerciseSessionMode.WORKOUT) {
            return null;
        }
        LocalDateTime startOfToday = LocalDateTime.now(SERVER_ZONE).toLocalDate().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);
        if (request.measurementGroupId() == null || request.measurementGroupId().isBlank()) {
            if (exerciseSessionRepository.existsByUserIdAndModeAndCreatedAtBetweenAndIsDeletedFalse(
                    userId, ExerciseSessionMode.MEASUREMENT, startOfToday, startOfTomorrow
            )) {
                throw new GeneralException(FitnessErrorCode.DAILY_MEASUREMENT_EXISTS);
            }
            return UUID.randomUUID().toString();
        }
        String groupId;
        try {
            groupId = UUID.fromString(request.measurementGroupId()).toString();
        } catch (IllegalArgumentException exception) {
            throw new GeneralException(FitnessErrorCode.INVALID_MEASUREMENT_GROUP_ID);
        }
        if (!exerciseSessionRepository.existsByUserIdAndModeAndMeasurementGroupIdAndCreatedAtBetweenAndIsDeletedFalse(
                userId, ExerciseSessionMode.MEASUREMENT, groupId, startOfToday, startOfTomorrow
        )) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_GROUP_NOT_FOUND);
        }
        if (exerciseSessionRepository.existsByUserIdAndMeasurementGroupIdAndExerciseTypeAndIsDeletedFalse(
                userId, groupId, request.exerciseType()
        )) {
            throw new GeneralException(FitnessErrorCode.EXERCISE_ALREADY_MEASURED);
        }
        return groupId;
    }

    private Map<String, Double> analyze(
            RuntimeExerciseSession runtime,
            List<com.atmd.backend.domain.fitness.dto.request.LandmarkDto> landmarks
    ) {
        if (runtime.getExerciseType() == ExerciseType.PUSH_UP) {
            PushUpMetrics metrics = pushUpAnalyzer.calculateMetrics(landmarks);
            pushUpAnalyzer.analyzeAndCount(runtime.getPushUpAnalysisState(), metrics);
            return Map.of(
                    "elbowAngle", metrics.elbowAngle(),
                    "bodyAlignmentAngle", metrics.bodyAlignmentAngle()
            );
        }

        if (runtime.getExerciseType() == ExerciseType.SIT_UP) {
            SitUpMetrics metrics = sitUpAnalyzer.calculateMetrics(landmarks);
            sitUpAnalyzer.analyzeAndCount(runtime.getSitUpAnalysisState(), metrics);
            return Map.of("trunkFlexionAngle", metrics.trunkFlexionAngle());
        }

        if (runtime.getExerciseType() == ExerciseType.PLANK) {
            PlankPhase previous = runtime.getPlankAnalysisState().getPhase();
            PlankMetrics metrics = plankAnalyzer.calculateMetrics(landmarks);
            PlankPhase current = plankAnalyzer.analyze(runtime.getPlankAnalysisState(), metrics);
            Instant now = Instant.now();
            if (previous == PlankPhase.POSITIONING && current == PlankPhase.HOLDING) {
                runtime.startPlankHolding(now);
            } else if (current == PlankPhase.BROKEN) {
                runtime.stopPlankHolding(now);
                completeRuntime(runtime, ExerciseSessionStatus.COMPLETED, now);
            }
            return Map.of(
                    "bodyAlignmentAngle", metrics.bodyAlignmentAngle(),
                    "legAlignmentAngle", metrics.legAlignmentAngle()
            );
        }

        ChairStandMetrics metrics = chairStandAnalyzer.calculateMetrics(landmarks);
        chairStandAnalyzer.analyzeAndCount(runtime.getAnalysisState(), metrics);
        return Map.of(
                "kneeAngle", metrics.kneeAngle(),
                "hipAngle", metrics.hipAngle()
        );
    }
}
