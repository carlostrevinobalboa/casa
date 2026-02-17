package com.casa.api.dto.recipe;

import java.util.List;
import java.util.UUID;

public record RecipeResponse(
    UUID id,
    String name,
    String description,
    String steps,
    List<RecipeIngredientResponse> ingredients
) {
}
