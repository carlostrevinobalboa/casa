package com.casa.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8, max = 120) String password,
    @NotBlank @Size(min = 2, max = 120) String displayName,
    @NotBlank @Size(min = 2, max = 120) String householdName
) {
}
