package com.casa.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.shopping.ShoppingListItemRequest;
import com.casa.api.dto.shopping.ShoppingListItemResponse;
import com.casa.domain.household.Household;
import com.casa.domain.pantry.PantryItem;
import com.casa.domain.shopping.ShoppingItemSourceType;
import com.casa.domain.shopping.ShoppingListItem;
import com.casa.repository.HouseholdRepository;
import com.casa.repository.ShoppingListItemRepository;

@Service
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;

    public ShoppingListService(
        ShoppingListItemRepository shoppingListItemRepository,
        HouseholdRepository householdRepository,
        HouseholdService householdService
    ) {
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
    }

    @Transactional(readOnly = true)
    public List<ShoppingListItemResponse> list(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);

        return shoppingListItemRepository.findByHouseholdIdOrderByPurchasedAscCreatedAtDesc(householdId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ShoppingListItemResponse addManual(UUID userId, UUID householdId, ShoppingListItemRequest request) {
        householdService.requireMembership(userId, householdId);

        ShoppingListItem item = upsertPendingItem(
            householdId,
            userId,
            request.productName(),
            request.quantity(),
            request.unit(),
            request.category(),
            ShoppingItemSourceType.MANUAL
        );

        return toResponse(item);
    }

    @Transactional
    public void addOrIncreaseFromRecipe(
        UUID householdId,
        UUID userId,
        String productName,
        double quantity,
        String unit,
        String category
    ) {
        upsertPendingItem(
            householdId,
            userId,
            productName,
            quantity,
            unit,
            category,
            ShoppingItemSourceType.RECIPE_SHORTAGE
        );
    }

    @Transactional
    public void syncFromPantryMinimum(UUID householdId, UUID userId, PantryItem pantryItem) {
        double missing = pantryItem.getMinimumQuantity() - pantryItem.getCurrentQuantity();
        if (missing <= 0.0) {
            return;
        }

        String productName = pantryItem.getProductName().trim();
        String unit = pantryItem.getUnit().trim();
        String category = pantryItem.getCategory().trim().toUpperCase();

        shoppingListItemRepository
            .findByHouseholdIdAndProductNameIgnoreCaseAndUnitIgnoreCaseAndSourceTypeAndPurchasedFalse(
                householdId,
                productName,
                unit,
                ShoppingItemSourceType.PANTRY_MINIMUM_LOW
            )
            .map(existing -> {
                existing.setQuantity(missing);
                existing.setCategory(category);
                return shoppingListItemRepository.save(existing);
            })
            .orElseGet(() -> upsertPendingItem(
                householdId,
                userId,
                productName,
                missing,
                unit,
                category,
                ShoppingItemSourceType.PANTRY_MINIMUM_LOW
            ));
    }

    @Transactional
    public ShoppingListItemResponse markPurchased(UUID userId, UUID householdId, UUID itemId, boolean purchased) {
        householdService.requireMembership(userId, householdId);

        ShoppingListItem item = shoppingListItemRepository.findByIdAndHouseholdId(itemId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item de compra no encontrado"));

        item.setPurchased(purchased);
        item.setPurchasedAt(purchased ? OffsetDateTime.now() : null);
        shoppingListItemRepository.save(item);

        return toResponse(item);
    }

    private ShoppingListItem upsertPendingItem(
        UUID householdId,
        UUID userId,
        String productName,
        double quantity,
        String unit,
        String category,
        ShoppingItemSourceType sourceType
    ) {
        String normalizedProductName = productName.trim();
        String normalizedUnit = unit.trim();
        String normalizedCategory = category.trim().toUpperCase();

        return shoppingListItemRepository
            .findByHouseholdIdAndProductNameIgnoreCaseAndUnitIgnoreCaseAndPurchasedFalse(
                householdId,
                normalizedProductName,
                normalizedUnit
            )
            .map(existing -> {
                existing.setQuantity(existing.getQuantity() + quantity);
                if (existing.getCategory() == null || existing.getCategory().isBlank()) {
                    existing.setCategory(normalizedCategory);
                }
                return shoppingListItemRepository.save(existing);
            })
            .orElseGet(() -> {
                Household household = householdRepository.findById(householdId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

                ShoppingListItem created = new ShoppingListItem();
                created.setHousehold(household);
                created.setProductName(normalizedProductName);
                created.setQuantity(quantity);
                created.setUnit(normalizedUnit);
                created.setCategory(normalizedCategory);
                created.setSourceType(sourceType);
                created.setAddedByUserId(userId);
                created.setPurchased(false);
                created.setPurchasedAt(null);
                return shoppingListItemRepository.save(created);
            });
    }

    private ShoppingListItemResponse toResponse(ShoppingListItem item) {
        return new ShoppingListItemResponse(
            item.getId(),
            item.getProductName(),
            item.getQuantity(),
            item.getUnit(),
            item.getCategory(),
            item.getSourceType(),
            item.isPurchased(),
            item.getPurchasedAt()
        );
    }
}
