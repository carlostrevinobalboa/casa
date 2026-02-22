package com.casa.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.auth.HouseholdSummary;
import com.casa.api.dto.household.CreateHouseholdRequest;
import com.casa.api.dto.household.HouseholdMemberResponse;
import com.casa.api.dto.household.JoinHouseholdRequest;
import com.casa.security.AppUserPrincipal;
import com.casa.service.AuthService;
import com.casa.service.HouseholdService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/households")
public class HouseholdController {

    private final AuthService authService;
    private final HouseholdService householdService;

    public HouseholdController(AuthService authService, HouseholdService householdService) {
        this.authService = authService;
        this.householdService = householdService;
    }

    @GetMapping
    public List<HouseholdSummary> list(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return authService.me(userId).households();
    }

    @GetMapping("/{householdId}/members")
    public List<HouseholdMemberResponse> members(Authentication authentication, @PathVariable UUID householdId) {
        return householdService.listMembers(currentUserId(authentication), householdId);
    }

    @PostMapping
    public List<HouseholdSummary> create(
        Authentication authentication,
        @Valid @RequestBody CreateHouseholdRequest request
    ) {
        UUID userId = currentUserId(authentication);
        householdService.createHousehold(userId, request.name());
        return authService.me(userId).households();
    }

    @PostMapping("/join")
    public List<HouseholdSummary> join(
        Authentication authentication,
        @Valid @RequestBody JoinHouseholdRequest request
    ) {
        UUID userId = currentUserId(authentication);
        householdService.joinHousehold(userId, request.inviteCode());
        return authService.me(userId).households();
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
