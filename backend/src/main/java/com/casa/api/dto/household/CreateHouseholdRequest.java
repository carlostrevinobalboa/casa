package com.casa.api.dto.household;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHouseholdRequest(
    @NotBlank @Size(min = 2, max = 120) String name
) {
}
