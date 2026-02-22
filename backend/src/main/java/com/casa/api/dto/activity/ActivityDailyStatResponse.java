package com.casa.api.dto.activity;

import java.time.LocalDate;

public record ActivityDailyStatResponse(
    LocalDate day,
    int activitiesCount,
    double distanceKm,
    long durationMinutes
) {
}
