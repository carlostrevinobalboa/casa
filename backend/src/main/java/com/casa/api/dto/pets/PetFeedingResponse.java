package com.casa.api.dto.pets;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PetFeedingResponse(
    UUID id,
    String foodType,
    double quantity,
    String unit,
    OffsetDateTime fedAt,
    String notes,
    UUID addedByUserId
) {
}
