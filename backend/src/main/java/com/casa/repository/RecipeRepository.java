package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.recipes.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    List<Recipe> findByHouseholdIdOrderByCreatedAtDesc(UUID householdId);

    Optional<Recipe> findByIdAndHouseholdId(UUID id, UUID householdId);
}
