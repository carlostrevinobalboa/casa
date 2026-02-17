package com.casa.api.dto.catalog;

import java.util.UUID;

public record CatalogCategoryResponse(
    UUID id,
    String name,
    boolean active
) {
}
