package com.casa.api.dto.shopping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ShoppingListItemRequest(
    @NotBlank @Size(max = 120) String productName,
    @NotNull @Positive Double quantity,
    @NotBlank @Size(max = 20) String unit,
    @NotBlank @Size(max = 60) String category
) {
}
