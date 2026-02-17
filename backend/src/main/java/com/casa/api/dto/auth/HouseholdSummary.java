package com.casa.api.dto.auth;

import java.util.UUID;
import com.casa.domain.household.HouseholdRole;

public record HouseholdSummary(
    UUID id,
    String name,
    String inviteCode,
    HouseholdRole role,
    String colorHex
) {
}
