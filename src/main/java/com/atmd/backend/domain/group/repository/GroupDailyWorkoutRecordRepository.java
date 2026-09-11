package com.atmd.backend.domain.group.repository;

import com.atmd.backend.domain.group.entity.GroupDailyWorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GroupDailyWorkoutRecordRepository extends JpaRepository<GroupDailyWorkoutRecord, Long> {

    @Query(value = """
            SELECT
                u.id AS userId,
                u.nickname AS nickname,
                (u.id = g.owner_id) AS isOwner,
                COALESCE(r.chair_stand_count, 0) AS chairStandCount,
                COALESCE(r.push_up_count, 0) AS pushUpCount,
                COALESCE(r.sit_up_count, 0) AS sitUpCount,
                COALESCE(r.plank_duration_ms, 0) AS plankDurationMs
            FROM group_users gu
            JOIN users u ON u.id = gu.user_id
            JOIN groups g ON g.id = gu.group_id
            LEFT JOIN group_daily_workout_records r
                ON r.group_id = gu.group_id
               AND r.user_id = gu.user_id
               AND r.workout_date = :workoutDate
            WHERE gu.group_id = :groupId
            ORDER BY u.id
            """, nativeQuery = true)
    List<GroupMemberWorkoutAggregateProjection> aggregateByGroupAndDate(
            @Param("groupId") Long groupId,
            @Param("workoutDate") LocalDate workoutDate
    );

    @Query(value = """
            SELECT
                r.user_id AS userId,
                r.workout_date AS workoutDate,
                r.chair_stand_count AS chairStandCount,
                r.push_up_count AS pushUpCount,
                r.sit_up_count AS sitUpCount,
                r.plank_duration_ms AS plankDurationMs
            FROM group_daily_workout_records r
            WHERE r.group_id = :groupId
              AND r.workout_date >= :startDate
              AND r.workout_date <  :endDateExclusive
            """, nativeQuery = true)
    List<GroupDailyRecordItemProjection> findItemsByGroupAndDateRange(
            @Param("groupId") Long groupId,
            @Param("startDate") LocalDate startDate,
            @Param("endDateExclusive") LocalDate endDateExclusive
    );

    @Modifying
    @Query(value = """
            INSERT INTO group_daily_workout_records (
                group_id, user_id, workout_date,
                chair_stand_count, push_up_count, sit_up_count, plank_duration_ms,
                created_at, updated_at
            ) VALUES (
                :groupId, :userId, :workoutDate,
                :chairStandCount, :pushUpCount, :sitUpCount, :plankDurationMs,
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
            ON CONFLICT (group_id, user_id, workout_date)
            DO UPDATE SET
                chair_stand_count = group_daily_workout_records.chair_stand_count + EXCLUDED.chair_stand_count,
                push_up_count = group_daily_workout_records.push_up_count + EXCLUDED.push_up_count,
                sit_up_count = group_daily_workout_records.sit_up_count + EXCLUDED.sit_up_count,
                plank_duration_ms = group_daily_workout_records.plank_duration_ms + EXCLUDED.plank_duration_ms,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void accumulate(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("workoutDate") LocalDate workoutDate,
            @Param("chairStandCount") long chairStandCount,
            @Param("pushUpCount") long pushUpCount,
            @Param("sitUpCount") long sitUpCount,
            @Param("plankDurationMs") long plankDurationMs
    );
}
