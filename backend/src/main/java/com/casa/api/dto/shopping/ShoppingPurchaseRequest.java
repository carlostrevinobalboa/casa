package com.casa.api.dto.shopping;

import jakarta.validation.constraints.DecimalMin;

public record ShoppingPurchaseRequest(
    @DecimalMin("0.0") Double totalPrice
) {
}
