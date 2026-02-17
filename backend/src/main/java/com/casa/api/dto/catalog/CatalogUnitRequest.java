package com.casa.api.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogUnitRequest(
    @NotBlank @Size(max = 20) String code,
    @NotBlank @Size(max = 80) String label,
    boolean active
) {
}
