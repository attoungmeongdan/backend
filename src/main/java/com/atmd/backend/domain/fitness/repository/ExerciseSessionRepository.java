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

    @Query(value = """
            with completed_groups as (
                select es.user_id,
                       es.measurement_group_id,
                       u.age as measurement_age,
                       max(case when es.exercise_type = 'CHAIR_STAND' then es.valid_count end) as chair_stand_value,
                       max(case when es.exercise_type = 'SIT_UP' then es.valid_count end) as sit_up_value,
                       max(case when es.exercise_type = 'PUSH_UP' then es.valid_count end) as push_up_value,
                       max(case when es.exercise_type = 'PLANK' then es.valid_duration_ms / 1000.0 end) as plank_value,
                       max(es.completed_at) as group_completed_at
                from exercise_session es
                join users u on u.id = es.user_id
                where es.session_mode = 'MEASUREMENT'
                  and es.status = 'COMPLETED'
                  and es.is_deleted = false
                  and es.measurement_group_id is not null
                  and u.is_deleted = false
                  and u.id <> :excludedUserId
                  and u.gender = :genderCode
                  and u.age between :minimumAge and :maximumAge
                group by es.user_id, es.measurement_group_id, u.age
                having count(distinct es.exercise_type) = 4
            ), latest_groups as (
                select completed_groups.*,
                       row_number() over (
                           partition by user_id
                           order by group_completed_at desc, measurement_group_id desc
                       ) as row_number
                from completed_groups
            )
            select measurement_age as "measurementAge",
                   chair_stand_value::double precision as "chairStandValue",
                   sit_up_value::double precision as "sitUpValue",
                   push_up_value::double precision as "pushUpValue",
                   plank_value::double precision as "plankValue"
            from latest_groups
            where row_number = 1
            """, nativeQuery = true)
    List<CohortMeasurementProjection> findLatestCompletedMeasurementsForCohort(
            @Param("genderCode") String genderCode,
            @Param("minimumAge") int minimumAge,
            @Param("maximumAge") int maximumAge,
            @Param("excludedUserId") Long excludedUserId
    );

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

    List<ExerciseSession> findAllByUserIdAndModeAndStatusAndCompletedAtGreaterThanEqualAndCompletedAtLessThanAndIsDeletedFalse(
            Long userId,
            ExerciseSessionMode mode,
            ExerciseSessionStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            select e.measurementGroupId as measurementGroupId,
                   max(e.completedAt) as completedAt
            from ExerciseSession e
            where e.user.id = :userId
              and e.mode = :mode
              and e.status = :status
              and e.measurementGroupId is not null
              and e.isDeleted = false
            group by e.measurementGroupId
            having count(distinct e.exerciseType) = 4
               and max(e.completedAt) >= :start
               and max(e.completedAt) < :end
            order by max(e.completedAt) asc
            """)
    List<CompletedMeasurementGroupProjection> findCompletedMeasurementGroupsInPeriod(
            @Param("userId") Long userId,
            @Param("mode") ExerciseSessionMode mode,
            @Param("status") ExerciseSessionStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
