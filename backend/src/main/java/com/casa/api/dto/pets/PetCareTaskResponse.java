package com.casa.api.dto.pets;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.casa.domain.pets.PetCareType;

public record PetCareTaskResponse(
    UUID id,
    PetCareType careType,
    String description,
    int frequencyDays,
    int notifyDaysBefore,
    OffsetDateTime lastPerformedAt,
    OffsetDateTime nextDueAt,
    boolean dueSoon,
    boolean active
) {
}
