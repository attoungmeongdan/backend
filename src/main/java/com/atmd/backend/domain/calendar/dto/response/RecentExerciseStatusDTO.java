package com.atmd.backend.domain.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record RecentExerciseStatusDTO(
        LocalDate date,
        String dayOfWeek,

        @JsonProperty("isCompleted")
        boolean isCompleted
) {}