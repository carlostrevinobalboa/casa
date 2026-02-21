package com.casa.api.dto.pets;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PetWeightResponse(
    UUID id,
    double weightKg,
    OffsetDateTime recordedAt,
    UUID addedByUserId
) {
}
