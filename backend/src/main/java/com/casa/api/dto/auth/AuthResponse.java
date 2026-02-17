package com.casa.api.dto.auth;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String tokenType,
    UserSummary user,
    List<HouseholdSummary> households,
    UUID activeHouseholdId
) {
}
