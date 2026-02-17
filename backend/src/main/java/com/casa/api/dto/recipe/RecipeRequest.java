package com.casa.api.dto.recipe;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RecipeRequest(
    @NotBlank @Size(max = 140) String name,
    @Size(max = 400) String description,
    @Size(max = 4000) String steps,
    @NotEmpty List<@Valid RecipeIngredientRequest> ingredients
) {
}
