package com.casa.api.dto.household;

import java.util.UUID;
import com.casa.domain.household.HouseholdRole;

public record HouseholdMemberResponse(
    UUID userId,
    String displayName,
    HouseholdRole role,
    String colorHex
) {
}
