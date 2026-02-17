package com.casa.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.pantry.PantryItemRequest;
import com.casa.api.dto.pantry.PantryItemResponse;
import com.casa.domain.household.Household;
import com.casa.domain.pantry.PantryItem;
import com.casa.repository.HouseholdRepository;
import com.casa.repository.PantryItemRepository;

@Service
public class PantryService {

    private static final int EXPIRING_SOON_DAYS = 3;

    private final PantryItemRepository pantryItemRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;
    private final PantryAlertService pantryAlertService;

    public PantryService(
        PantryItemRepository pantryItemRepository,
        HouseholdRepository householdRepository,
        HouseholdService householdService,
        PantryAlertService pantryAlertService
    ) {
        this.pantryItemRepository = pantryItemRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
        this.pantryAlertService = pantryAlertService;
    }

    @Transactional(readOnly = true)
    public List<PantryItemResponse> list(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);

        return pantryItemRepository.findByHouseholdIdOrderByProductNameAsc(householdId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public PantryItemResponse create(UUID userId, UUID householdId, PantryItemRequest request) {
        householdService.requireMembership(userId, householdId);

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        PantryItem item = new PantryItem();
        item.setHousehold(household);
        apply(item, request);
        pantryItemRepository.save(item);

        pantryAlertService.evaluateAndNotify(item);

        return toResponse(item);
    }

    @Transactional
    public PantryItemResponse update(UUID userId, UUID householdId, UUID itemId, PantryItemRequest request) {
        householdService.requireMembership(userId, householdId);

        PantryItem item = pantryItemRepository.findByIdAndHouseholdId(itemId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        apply(item, request);
        pantryItemRepository.save(item);

        pantryAlertService.evaluateAndNotify(item);

        return toResponse(item);
    }

    @Transactional
    public void delete(UUID userId, UUID householdId, UUID itemId) {
        householdService.requireMembership(userId, householdId);

        PantryItem item = pantryItemRepository.findByIdAndHouseholdId(itemId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        pantryItemRepository.delete(item);
    }

    private void apply(PantryItem item, PantryItemRequest request) {
        item.setProductName(request.productName().trim());
        item.setCurrentQuantity(request.currentQuantity());
        item.setMinimumQuantity(request.minimumQuantity());
        item.setUnit(request.unit().trim());
        item.setExpirationDate(request.expirationDate());
        item.setCategory(request.category().trim().toUpperCase());
    }

    private PantryItemResponse toResponse(PantryItem item) {
        return new PantryItemResponse(
            item.getId(),
            item.getProductName(),
            item.getCurrentQuantity(),
            item.getMinimumQuantity(),
            item.getUnit(),
            item.getExpirationDate(),
            item.getCategory(),
            item.getCurrentQuantity() <= item.getMinimumQuantity(),
            isExpiringSoon(item.getExpirationDate())
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
