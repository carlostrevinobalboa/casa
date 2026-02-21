package com.casa.api.dto.pets;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PetFeedingRequest(
    @NotBlank @Size(max = 120) String foodType,
    @NotNull @Positive Double quantity,
    @NotBlank @Size(max = 20) String unit,
    OffsetDateTime fedAt,
    @Size(max = 300) String notes
) {
}
