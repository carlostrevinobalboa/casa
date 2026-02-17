package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.shopping.ShoppingItemSourceType;
import com.casa.domain.shopping.ShoppingListItem;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, UUID> {

    List<ShoppingListItem> findByHouseholdIdOrderByPurchasedAscCreatedAtDesc(UUID householdId);

    Optional<ShoppingListItem> findByIdAndHouseholdId(UUID id, UUID householdId);

    Optional<ShoppingListItem> findByHouseholdIdAndProductNameIgnoreCaseAndUnitIgnoreCaseAndPurchasedFalse(
        UUID householdId,
        String productName,
        String unit
    );

    Optional<ShoppingListItem> findByHouseholdIdAndProductNameIgnoreCaseAndUnitIgnoreCaseAndSourceTypeAndPurchasedFalse(
        UUID householdId,
        String productName,
        String unit,
        ShoppingItemSourceType sourceType
    );
}
