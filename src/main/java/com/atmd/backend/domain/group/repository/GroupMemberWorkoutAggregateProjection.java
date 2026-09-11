package com.atmd.backend.domain.group.repository;

public interface GroupMemberWorkoutAggregateProjection {
    Long getUserId();
    String getNickname();
    Boolean getIsOwner();
    Long getChairStandCount();
    Long getPushUpCount();
    Long getSitUpCount();
    Long getPlankDurationMs();
}
