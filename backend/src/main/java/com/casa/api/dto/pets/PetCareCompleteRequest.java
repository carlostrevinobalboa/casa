package com.casa.api.dto.pets;

import java.time.OffsetDateTime;

public record PetCareCompleteRequest(
    OffsetDateTime performedAt
) {
}
