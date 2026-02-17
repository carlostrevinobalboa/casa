package com.casa.api.dto.recipe;

import java.util.UUID;

public record RecipeIngredientResponse(
    UUID id,
    String productName,
    double requiredQuantity,
    String unit
) {
}
