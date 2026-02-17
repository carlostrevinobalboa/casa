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
import com.casa.api.dto.recipe.RecipeExecutionHistoryResponse;
import com.casa.api.dto.recipe.RecipeExecutionResponse;
import com.casa.api.dto.recipe.RecipeRequest;
import com.casa.api.dto.recipe.RecipeResponse;
import com.casa.security.AppUserPrincipal;
import com.casa.service.RecipeService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/households/{householdId}/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public List<RecipeResponse> list(Authentication authentication, @PathVariable UUID householdId) {
        return recipeService.list(currentUserId(authentication), householdId);
    }

    @PostMapping
    public RecipeResponse create(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody RecipeRequest request
    ) {
        return recipeService.create(currentUserId(authentication), householdId, request);
    }

    @PutMapping("/{recipeId}")
    public RecipeResponse update(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID recipeId,
        @Valid @RequestBody RecipeRequest request
    ) {
        return recipeService.update(currentUserId(authentication), householdId, recipeId, request);
    }

    @DeleteMapping("/{recipeId}")
    public void delete(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID recipeId
    ) {
        recipeService.delete(currentUserId(authentication), householdId, recipeId);
    }

    @PostMapping("/{recipeId}/execute")
    public RecipeExecutionResponse execute(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID recipeId
    ) {
        return recipeService.execute(currentUserId(authentication), householdId, recipeId);
    }

    @GetMapping("/executions")
    public List<RecipeExecutionHistoryResponse> history(Authentication authentication, @PathVariable UUID householdId) {
        return recipeService.history(currentUserId(authentication), householdId);
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
