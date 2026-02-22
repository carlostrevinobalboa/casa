package com.casa.api.dto.activity;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record ActivityPointRequest(
    @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
    @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
    OffsetDateTime recordedAt
) {
}
