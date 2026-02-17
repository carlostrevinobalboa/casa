package com.casa.api.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogCategoryRequest(
    @NotBlank @Size(max = 80) String name,
    boolean active
) {
}
