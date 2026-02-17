package com.casa.api.dto.catalog;

import java.util.UUID;

public record CatalogProductResponse(
    UUID id,
    String name,
    String defaultUnitCode,
    String defaultCategoryName,
    boolean active
) {
}
