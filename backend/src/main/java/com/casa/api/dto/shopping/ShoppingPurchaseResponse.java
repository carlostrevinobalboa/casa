package com.casa.api.dto.shopping;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ShoppingPurchaseResponse(
    UUID id,
    UUID shoppingListItemId,
    String productName,
    double quantity,
    String unit,
    String category,
    double totalPrice,
    Double unitPrice,
    UUID purchasedByUserId,
    OffsetDateTime purchasedAt
) {
}
