package com.casa.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.recipes.RecipeIngredient;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, UUID> {

    List<RecipeIngredient> findByRecipeIdOrderByCreatedAtAsc(UUID recipeId);

    void deleteByRecipeId(UUID recipeId);
}
