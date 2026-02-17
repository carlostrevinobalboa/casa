package com.casa.api.dto.recipe;

public record RecipeExecutionShortageResponse(
    String productName,
    double missingQuantity,
    String unit
) {
}
