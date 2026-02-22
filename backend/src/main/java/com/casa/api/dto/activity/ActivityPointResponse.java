package com.casa.api.dto.activity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityPointResponse(
    UUID id,
    int sequenceNumber,
    double latitude,
    double longitude,
    OffsetDateTime recordedAt
) {
}
