package com.casa.api.dto.recipe;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RecipeExecutionHistoryResponse(
    UUID executionId,
    UUID recipeId,
    String recipeName,
    UUID executedByUserId,
    int shortagesCount,
    OffsetDateTime executedAt
) {
}
