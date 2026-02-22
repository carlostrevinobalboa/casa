package com.casa.api.dto.activity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.casa.domain.activities.ActivityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ActivityRequest(
    @NotNull ActivityType type,
    UUID performedByUserId,
    UUID petId,
    @NotNull OffsetDateTime startedAt,
    @NotNull OffsetDateTime endedAt,
    @PositiveOrZero Double distanceKm,
    Boolean gpsTracked,
    @Size(max = 120) String title,
    @Size(max = 600) String notes,
    List<@Valid ActivityPointRequest> points
) {
}
