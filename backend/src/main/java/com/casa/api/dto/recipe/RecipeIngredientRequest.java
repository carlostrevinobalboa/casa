package com.casa.api.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecipeIngredientRequest(
    @NotBlank @Size(max = 120) String productName,
    @NotNull @Positive Double requiredQuantity,
    @NotBlank @Size(max = 20) String unit
) {
}
