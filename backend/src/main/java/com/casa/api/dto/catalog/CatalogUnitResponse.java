package com.casa.api.dto.catalog;

import java.util.UUID;

public record CatalogUnitResponse(
    UUID id,
    String code,
    String label,
    boolean active
) {
}
