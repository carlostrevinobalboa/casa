package com.casa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.recipe.RecipeExecutionHistoryResponse;
import com.casa.api.dto.recipe.RecipeExecutionResponse;
import com.casa.api.dto.recipe.RecipeExecutionShortageResponse;
import com.casa.api.dto.recipe.RecipeIngredientRequest;
import com.casa.api.dto.recipe.RecipeIngredientResponse;
import com.casa.api.dto.recipe.RecipeRequest;
import com.casa.api.dto.recipe.RecipeResponse;
import com.casa.domain.household.Household;
import com.casa.domain.pantry.PantryItem;
import com.casa.domain.recipes.Recipe;
import com.casa.domain.recipes.RecipeExecution;
import com.casa.domain.recipes.RecipeIngredient;
import com.casa.repository.HouseholdRepository;
import com.casa.repository.PantryItemRepository;
import com.casa.repository.RecipeExecutionRepository;
import com.casa.repository.RecipeIngredientRepository;
import com.casa.repository.RecipeRepository;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeExecutionRepository recipeExecutionRepository;
    private final HouseholdRepository householdRepository;
    private final PantryItemRepository pantryItemRepository;
    private final HouseholdService householdService;
    private final PantryAlertService pantryAlertService;
    private final ShoppingListService shoppingListService;

    public RecipeService(
        RecipeRepository recipeRepository,
        RecipeIngredientRepository recipeIngredientRepository,
        RecipeExecutionRepository recipeExecutionRepository,
        HouseholdRepository householdRepository,
        PantryItemRepository pantryItemRepository,
        HouseholdService householdService,
        PantryAlertService pantryAlertService,
        ShoppingListService shoppingListService
    ) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeExecutionRepository = recipeExecutionRepository;
        this.householdRepository = householdRepository;
        this.pantryItemRepository = pantryItemRepository;
        this.householdService = householdService;
        this.pantryAlertService = pantryAlertService;
        this.shoppingListService = shoppingListService;
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> list(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);

        return recipeRepository.findByHouseholdIdOrderByCreatedAtDesc(householdId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public RecipeResponse create(UUID userId, UUID householdId, RecipeRequest request) {
        householdService.requireMembership(userId, householdId);

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        Recipe recipe = new Recipe();
        recipe.setHousehold(household);
        recipe.setName(request.name().trim());
        recipe.setDescription(cleanNullableText(request.description()));
        recipe.setSteps(cleanNullableText(request.steps()));
        recipe.setCreatedByUserId(userId);
        recipeRepository.save(recipe);

        saveIngredients(recipe, request.ingredients());

        return toResponse(recipe);
    }

    @Transactional
    public RecipeResponse update(UUID userId, UUID householdId, UUID recipeId, RecipeRequest request) {
        householdService.requireMembership(userId, householdId);

        Recipe recipe = recipeRepository.findByIdAndHouseholdId(recipeId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receta no encontrada"));

        recipe.setName(request.name().trim());
        recipe.setDescription(cleanNullableText(request.description()));
        recipe.setSteps(cleanNullableText(request.steps()));
        recipeRepository.save(recipe);

        recipeIngredientRepository.deleteByRecipeId(recipeId);
        saveIngredients(recipe, request.ingredients());

        return toResponse(recipe);
    }

    @Transactional
    public void delete(UUID userId, UUID householdId, UUID recipeId) {
        householdService.requireMembership(userId, householdId);

        Recipe recipe = recipeRepository.findByIdAndHouseholdId(recipeId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receta no encontrada"));

        if (recipeExecutionRepository.existsByRecipeId(recipeId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede eliminar una receta con historial de ejecuciones"
            );
        }

        recipeIngredientRepository.deleteByRecipeId(recipeId);
        recipeRepository.delete(recipe);
    }

    @Transactional
    public RecipeExecutionResponse execute(UUID userId, UUID householdId, UUID recipeId) {
        householdService.requireMembership(userId, householdId);

        Recipe recipe = recipeRepository.findByIdAndHouseholdId(recipeId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receta no encontrada"));

        List<RecipeIngredient> ingredients = recipeIngredientRepository.findByRecipeIdOrderByCreatedAtAsc(recipeId);
        List<RecipeExecutionShortageResponse> shortages = new ArrayList<>();

        for (RecipeIngredient ingredient : ingredients) {
            PantryItem pantryItem = pantryItemRepository
                .findByHouseholdIdAndProductNameIgnoreCase(householdId, ingredient.getProductName())
                .orElse(null);

            double requested = ingredient.getRequiredQuantity();
            double available = pantryItem == null ? 0.0 : pantryItem.getCurrentQuantity();
            double used = Math.min(available, requested);
            double missing = Math.max(0.0, requested - used);

            if (pantryItem != null && used > 0.0) {
                pantryItem.setCurrentQuantity(available - used);
                pantryItemRepository.save(pantryItem);
                pantryAlertService.evaluateAndNotify(pantryItem);
            }

            if (missing > 0.0) {
                shoppingListService.addOrIncreaseFromRecipe(
                    householdId,
                    userId,
                    ingredient.getProductName(),
                    missing,
                    ingredient.getUnit(),
                    "GENERAL"
                );

                shortages.add(new RecipeExecutionShortageResponse(
                    ingredient.getProductName(),
                    missing,
                    ingredient.getUnit()
                ));
            }
        }

        RecipeExecution execution = new RecipeExecution();
        execution.setHousehold(recipe.getHousehold());
        execution.setRecipe(recipe);
        execution.setExecutedByUserId(userId);
        execution.setShortagesCount(shortages.size());
        recipeExecutionRepository.save(execution);

        return new RecipeExecutionResponse(
            execution.getId(),
            recipe.getId(),
            recipe.getName(),
            shortages.size(),
            shortages
        );
    }

    @Transactional(readOnly = true)
    public List<RecipeExecutionHistoryResponse> history(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);

        return recipeExecutionRepository.findTop50ByHouseholdIdOrderByCreatedAtDesc(householdId)
            .stream()
            .map(execution -> new RecipeExecutionHistoryResponse(
                execution.getId(),
                execution.getRecipe().getId(),
                execution.getRecipe().getName(),
                execution.getExecutedByUserId(),
                execution.getShortagesCount(),
                execution.getCreatedAt()
            ))
            .toList();
    }

    private void saveIngredients(Recipe recipe, List<RecipeIngredientRequest> ingredients) {
        List<RecipeIngredient> entities = ingredients.stream()
            .map(request -> {
                RecipeIngredient entity = new RecipeIngredient();
                entity.setRecipe(recipe);
                entity.setProductName(request.productName().trim());
                entity.setRequiredQuantity(request.requiredQuantity());
                entity.setUnit(request.unit().trim());
                return entity;
            })
            .toList();

        recipeIngredientRepository.saveAll(entities);
    }

    private RecipeResponse toResponse(Recipe recipe) {
        List<RecipeIngredientResponse> ingredients = recipeIngredientRepository.findByRecipeIdOrderByCreatedAtAsc(recipe.getId())
            .stream()
            .map(ingredient -> new RecipeIngredientResponse(
                ingredient.getId(),
                ingredient.getProductName(),
                ingredient.getRequiredQuantity(),
                ingredient.getUnit()
            ))
            .toList();

        return new RecipeResponse(
            recipe.getId(),
            recipe.getName(),
            recipe.getDescription(),
            recipe.getSteps(),
            ingredients
        );
    }

    private String cleanNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
