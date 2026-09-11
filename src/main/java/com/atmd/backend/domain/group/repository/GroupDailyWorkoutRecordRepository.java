package com.atmd.backend.domain.group.repository;

import com.atmd.backend.domain.group.entity.GroupDailyWorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface GroupDailyWorkoutRecordRepository extends JpaRepository<GroupDailyWorkoutRecord, Long> {

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
