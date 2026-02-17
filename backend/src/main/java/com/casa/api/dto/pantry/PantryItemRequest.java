package com.casa.api.dto.pantry;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PantryItemRequest(
    @NotBlank @Size(max = 120) String productName,
    @NotNull @PositiveOrZero Double currentQuantity,
    @NotNull @PositiveOrZero Double minimumQuantity,
    @NotBlank @Size(max = 20) String unit,
    LocalDate expirationDate,
    @NotBlank @Size(max = 60) String category
) {
}
