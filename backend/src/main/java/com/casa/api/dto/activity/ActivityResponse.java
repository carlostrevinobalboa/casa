package com.casa.api.dto.activity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.casa.domain.activities.ActivityType;

public record ActivityResponse(
    UUID id,
    ActivityType type,
    UUID performedByUserId,
    UUID petId,
    OffsetDateTime startedAt,
    OffsetDateTime endedAt,
    long durationSeconds,
    double distanceKm,
    String title,
    String notes,
    boolean gpsTracked,
    List<ActivityPointResponse> points
) {
}
