package com.casa.service;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.casa.domain.notifications.NotificationType;
import com.casa.domain.pantry.PantryItem;
import com.casa.repository.PantryItemRepository;

@Service
public class PantryAlertService {

    private static final int EXPIRING_SOON_DAYS = 3;

    private final HouseholdService householdService;
    private final NotificationService notificationService;
    private final PantryItemRepository pantryItemRepository;
    private final ShoppingListService shoppingListService;

    public PantryAlertService(
        HouseholdService householdService,
        NotificationService notificationService,
        PantryItemRepository pantryItemRepository,
        ShoppingListService shoppingListService
    ) {
        this.householdService = householdService;
        this.notificationService = notificationService;
        this.pantryItemRepository = pantryItemRepository;
        this.shoppingListService = shoppingListService;
    }

    @Transactional
    public void evaluateAndNotify(PantryItem item) {
        UUID householdId = item.getHousehold().getId();

        if (item.getCurrentQuantity() <= item.getMinimumQuantity()) {
            shoppingListService.syncFromPantryMinimum(
                householdId,
                item.getHousehold().getCreatedByUserId(),
                item
            );

            String title = "Stock bajo: " + item.getProductName();
            String body = "Cantidad actual " + item.getCurrentQuantity() + " " + item.getUnit() +
                ", minimo " + item.getMinimumQuantity() + " " + item.getUnit() + ".";

            notifyHousehold(
                householdId,
                NotificationType.PANTRY_MINIMUM_LOW,
                title,
                body,
                item.getId()
            );
        }

        if (isExpiringSoon(item.getExpirationDate())) {
            String title = "Caducidad proxima: " + item.getProductName();
            String body = "Caduca el " + item.getExpirationDate() + ".";

            notifyHousehold(
                householdId,
                NotificationType.PANTRY_EXPIRING_SOON,
                title,
                body,
                item.getId()
            );
        }
    }

    @Transactional
    public void scanAllItemsAndNotify() {
        pantryItemRepository.findAll().forEach(this::evaluateAndNotify);
    }

    private void notifyHousehold(
        UUID householdId,
        NotificationType type,
        String title,
        String body,
        UUID pantryItemId
    ) {
        householdService.householdUserIds(householdId).forEach(userId ->
            notificationService.createIfMissing(
                householdId,
                userId,
                type,
                title,
                body,
                "PANTRY_ITEM",
                pantryItemId
            )
        );
    }

    private boolean isExpiringSoon(LocalDate expirationDate) {
        if (expirationDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(EXPIRING_SOON_DAYS);

        return !expirationDate.isBefore(today) && !expirationDate.isAfter(threshold);
    }
}
