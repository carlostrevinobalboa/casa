package com.casa.api.dto.pets;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PetRequest(
    @NotBlank @Size(max = 80) String name,
    @NotBlank @Size(max = 40) String type,
    @Size(max = 80) String chipCode,
    @Size(max = 120) String veterinarian,
    @Size(max = 400) String photoUrl,
    @PositiveOrZero Double currentWeightKg,
    @Size(max = 120) String foodName,
    @PositiveOrZero Double foodStockQuantity,
    @PositiveOrZero Double foodDailyConsumptionQuantity,
    @Size(max = 20) String foodUnit,
    boolean active
) {
}
