package com.casa.security;

import java.util.UUID;

public record AppUserPrincipal(
    UUID userId,
    String email,
    String displayName
) {
}
