package com.casa.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.recipes.RecipeExecution;

public interface RecipeExecutionRepository extends JpaRepository<RecipeExecution, UUID> {

    List<RecipeExecution> findTop50ByHouseholdIdOrderByCreatedAtDesc(UUID householdId);

    boolean existsByRecipeId(UUID recipeId);
}
