package com.casa.api.dto.pets;

import java.util.UUID;

public record PetResponse(
    UUID id,
    String name,
    String type,
    String chipCode,
    String veterinarian,
    String photoUrl,
    Double currentWeightKg,
    String foodName,
    Double foodStockQuantity,
    Double foodDailyConsumptionQuantity,
    String foodUnit,
    Double foodDaysRemaining,
    boolean foodLow,
    boolean active
) {
}
