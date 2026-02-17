package com.casa.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.pantry.PantryItemRequest;
import com.casa.api.dto.pantry.PantryItemResponse;
import com.casa.security.AppUserPrincipal;
import com.casa.service.PantryService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/households/{householdId}/pantry-items")
public class PantryController {

    private final PantryService pantryService;

    public PantryController(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    @GetMapping
    public List<PantryItemResponse> list(Authentication authentication, @PathVariable UUID householdId) {
        return pantryService.list(currentUserId(authentication), householdId);
    }

    @PostMapping
    public PantryItemResponse create(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody PantryItemRequest request
    ) {
        return pantryService.create(currentUserId(authentication), householdId, request);
    }

    @PutMapping("/{itemId}")
    public PantryItemResponse update(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID itemId,
        @Valid @RequestBody PantryItemRequest request
    ) {
        return pantryService.update(currentUserId(authentication), householdId, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID itemId
    ) {
        pantryService.delete(currentUserId(authentication), householdId, itemId);
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
