package com.casa.api.dto.auth;

import java.util.List;
import java.util.UUID;

public record MeResponse(
    UserSummary user,
    List<HouseholdSummary> households,
    UUID activeHouseholdId
) {
}
