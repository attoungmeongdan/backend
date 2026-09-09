package com.atmd.backend.domain.fitness.repository;

import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

public interface ExerciseSessionRepository extends JpaRepository<ExerciseSession, Long> {
    void deleteAllByUserId(Long userId);

    List<ExerciseSession> findAllByUserIdAndStatusIn(Long userId, Collection<ExerciseSessionStatus> statuses);

    boolean existsByUserIdAndModeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalse(
            Long userId, ExerciseSessionMode mode, LocalDateTime start, LocalDateTime end
    );

    boolean existsByUserIdAndModeAndMeasurementGroupIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalse(
            Long userId, ExerciseSessionMode mode, String groupId, LocalDateTime start, LocalDateTime end
    );

    boolean existsByUserIdAndMeasurementGroupIdAndExerciseTypeAndStatusAndIsDeletedFalse(
            Long userId, String groupId, ExerciseType exerciseType, ExerciseSessionStatus status
    );

    List<ExerciseSession> findAllByUserIdAndModeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalseOrderByCreatedAtAsc(
            Long userId, ExerciseSessionMode mode, LocalDateTime start, LocalDateTime end
    );

    List<ExerciseSession> findAllByUserIdAndMeasurementGroupIdAndIsDeletedFalse(Long userId, String groupId);

    @Query("""
            select e.measurementGroupId
            from ExerciseSession e
            where e.user.id = :userId
              and e.mode = :mode
              and e.status = :status
              and e.measurementGroupId is not null
              and e.isDeleted = false
            group by e.measurementGroupId
            having count(distinct e.exerciseType) = 4
            order by max(e.completedAt) desc
            """)
    List<String> findRecentCompletedMeasurementGroupIds(
            @Param("userId") Long userId,
            @Param("mode") ExerciseSessionMode mode,
            @Param("status") ExerciseSessionStatus status,
            Pageable pageable
    );

    List<ExerciseSession> findAllByUserIdAndMeasurementGroupIdInAndStatusAndIsDeletedFalse(
            Long userId, Collection<String> groupIds, ExerciseSessionStatus status
    );
}
