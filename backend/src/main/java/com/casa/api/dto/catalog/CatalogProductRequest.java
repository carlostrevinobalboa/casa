package com.casa.api.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogProductRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 20) String defaultUnitCode,
    @Size(max = 80) String defaultCategoryName,
    boolean active
) {
}
