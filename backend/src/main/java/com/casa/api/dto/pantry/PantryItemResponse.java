package com.casa.api.dto.pantry;

import java.time.LocalDate;
import java.util.UUID;

public record PantryItemResponse(
    UUID id,
    String productName,
    double currentQuantity,
    double minimumQuantity,
    String unit,
    LocalDate expirationDate,
    String category,
    boolean belowMinimum,
    boolean expiresSoon
) {
}
