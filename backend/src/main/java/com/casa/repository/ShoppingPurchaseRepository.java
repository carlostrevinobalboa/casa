package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.shopping.ShoppingPurchase;

public interface ShoppingPurchaseRepository extends JpaRepository<ShoppingPurchase, UUID> {

    List<ShoppingPurchase> findTop100ByHouseholdIdOrderByPurchasedAtDesc(UUID householdId);

    Optional<ShoppingPurchase> findTopByHouseholdIdAndShoppingListItemIdOrderByPurchasedAtDesc(
        UUID householdId,
        UUID shoppingListItemId
    );
}
