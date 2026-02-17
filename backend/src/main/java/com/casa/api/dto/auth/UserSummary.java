package com.casa.api.dto.auth;

import java.util.UUID;

public record UserSummary(
    UUID id,
    String email,
    String displayName
) {
}
