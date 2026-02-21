package com.casa.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.shopping.ShoppingPurchaseRequest;
import com.casa.api.dto.shopping.ShoppingPurchaseResponse;
import com.casa.api.dto.shopping.ShoppingListItemRequest;
import com.casa.api.dto.shopping.ShoppingListItemResponse;
import com.casa.security.AppUserPrincipal;
import com.casa.service.ShoppingListService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/households/{householdId}/shopping-list-items")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @GetMapping
    public List<ShoppingListItemResponse> list(Authentication authentication, @PathVariable UUID householdId) {
        return shoppingListService.list(currentUserId(authentication), householdId);
    }

    @GetMapping("/history")
    public List<ShoppingPurchaseResponse> history(Authentication authentication, @PathVariable UUID householdId) {
        return shoppingListService.listPurchaseHistory(currentUserId(authentication), householdId);
    }

    @PostMapping
    public ShoppingListItemResponse addManual(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody ShoppingListItemRequest request
    ) {
        return shoppingListService.addManual(currentUserId(authentication), householdId, request);
    }

    @PostMapping("/{itemId}/purchase")
    public ShoppingListItemResponse purchase(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID itemId,
        @RequestBody(required = false) @Valid ShoppingPurchaseRequest request
    ) {
        Double totalPrice = request == null ? null : request.totalPrice();
        return shoppingListService.markPurchased(currentUserId(authentication), householdId, itemId, true, totalPrice);
    }

    @PostMapping("/{itemId}/unpurchase")
    public ShoppingListItemResponse unpurchase(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID itemId
    ) {
        return shoppingListService.markPurchased(currentUserId(authentication), householdId, itemId, false, null);
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
