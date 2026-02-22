package com.casa.api.dto.activity;

import java.time.LocalDate;
import java.util.List;

public record ActivityWeeklyStatsResponse(
    LocalDate weekStart,
    LocalDate weekEnd,
    int activitiesCount,
    double distanceKm,
    long durationMinutes,
    List<ActivityDailyStatResponse> daily
) {
}
