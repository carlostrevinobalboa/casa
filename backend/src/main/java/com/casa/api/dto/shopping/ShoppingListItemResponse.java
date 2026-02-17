package com.casa.api.dto.shopping;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.casa.domain.shopping.ShoppingItemSourceType;

public record ShoppingListItemResponse(
    UUID id,
    String productName,
    double quantity,
    String unit,
    String category,
    ShoppingItemSourceType sourceType,
    boolean purchased,
    OffsetDateTime purchasedAt
) {
}
