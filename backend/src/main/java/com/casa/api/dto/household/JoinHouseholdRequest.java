package com.casa.api.dto.household;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinHouseholdRequest(
    @NotBlank @Size(min = 6, max = 16) String inviteCode
) {
}
