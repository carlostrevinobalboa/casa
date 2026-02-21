package com.casa.api.dto.pets;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PetWeightRequest(
    @NotNull @Positive Double weightKg,
    OffsetDateTime recordedAt
) {
}
