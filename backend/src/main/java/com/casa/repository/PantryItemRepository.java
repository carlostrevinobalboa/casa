package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.pantry.PantryItem;

public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {

    List<PantryItem> findByHouseholdIdOrderByProductNameAsc(UUID householdId);

    Optional<PantryItem> findByIdAndHouseholdId(UUID id, UUID householdId);

    Optional<PantryItem> findByHouseholdIdAndProductNameIgnoreCase(UUID householdId, String productName);
}
