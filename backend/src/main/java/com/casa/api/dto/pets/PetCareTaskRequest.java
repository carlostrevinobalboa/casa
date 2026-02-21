package com.casa.api.dto.pets;

import java.time.OffsetDateTime;
import com.casa.domain.pets.PetCareType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PetCareTaskRequest(
    @NotNull PetCareType careType,
    @Size(max = 200) String description,
    @NotNull @Min(1) @Max(3650) Integer frequencyDays,
    @Min(0) @Max(30) Integer notifyDaysBefore,
    OffsetDateTime lastPerformedAt,
    boolean active
) {
}
