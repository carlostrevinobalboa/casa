package com.casa.api.dto.recipe;

import java.util.List;
import java.util.UUID;

public record RecipeExecutionResponse(
    UUID executionId,
    UUID recipeId,
    String recipeName,
    int shortagesCount,
    List<RecipeExecutionShortageResponse> shortages
) {
}
