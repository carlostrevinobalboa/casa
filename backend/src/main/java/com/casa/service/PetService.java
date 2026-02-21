package com.casa.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.pets.PetCareTaskRequest;
import com.casa.api.dto.pets.PetCareTaskResponse;
import com.casa.api.dto.pets.PetFeedingRequest;
import com.casa.api.dto.pets.PetFeedingResponse;
import com.casa.api.dto.pets.PetRequest;
import com.casa.api.dto.pets.PetResponse;
import com.casa.api.dto.pets.PetWeightRequest;
import com.casa.api.dto.pets.PetWeightResponse;
import com.casa.domain.household.Household;
import com.casa.domain.notifications.NotificationType;
import com.casa.domain.pets.Pet;
import com.casa.domain.pets.PetCareTask;
import com.casa.domain.pets.PetCareType;
import com.casa.domain.pets.PetFeedingRecord;
import com.casa.domain.pets.PetWeightRecord;
import com.casa.repository.HouseholdRepository;
import com.casa.repository.PetCareTaskRepository;
import com.casa.repository.PetFeedingRecordRepository;
import com.casa.repository.PetRepository;
import com.casa.repository.PetWeightRecordRepository;

@Service
public class PetService {

    private static final int FOOD_LOW_THRESHOLD_DAYS = 3;

    private final PetRepository petRepository;
    private final PetFeedingRecordRepository petFeedingRecordRepository;
    private final PetWeightRecordRepository petWeightRecordRepository;
    private final PetCareTaskRepository petCareTaskRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;
    private final NotificationService notificationService;
    private final ShoppingListService shoppingListService;

    public PetService(
        PetRepository petRepository,
        PetFeedingRecordRepository petFeedingRecordRepository,
        PetWeightRecordRepository petWeightRecordRepository,
        PetCareTaskRepository petCareTaskRepository,
        HouseholdRepository householdRepository,
        HouseholdService householdService,
        NotificationService notificationService,
        ShoppingListService shoppingListService
    ) {
        this.petRepository = petRepository;
        this.petFeedingRecordRepository = petFeedingRecordRepository;
        this.petWeightRecordRepository = petWeightRecordRepository;
        this.petCareTaskRepository = petCareTaskRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
        this.notificationService = notificationService;
        this.shoppingListService = shoppingListService;
    }

    @Transactional(readOnly = true)
    public List<PetResponse> listPets(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);

        return petRepository.findByHouseholdIdOrderByNameAsc(householdId)
            .stream()
            .map(this::toPetResponse)
            .toList();
    }

    @Transactional
    public PetResponse createPet(UUID userId, UUID householdId, PetRequest request) {
        householdService.requireMembership(userId, householdId);

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        Pet pet = new Pet();
        pet.setHousehold(household);
        pet.setCreatedByUserId(userId);
        applyPet(pet, request);
        petRepository.save(pet);

        evaluatePetFoodAndNotify(pet);

        return toPetResponse(pet);
    }

    @Transactional
    public PetResponse updatePet(UUID userId, UUID householdId, UUID petId, PetRequest request) {
        householdService.requireMembership(userId, householdId);

        Pet pet = requirePet(householdId, petId);
        applyPet(pet, request);
        petRepository.save(pet);

        evaluatePetFoodAndNotify(pet);

        return toPetResponse(pet);
    }

    @Transactional
    public void deletePet(UUID userId, UUID householdId, UUID petId) {
        householdService.requireMembership(userId, householdId);
        Pet pet = requirePet(householdId, petId);

        petCareTaskRepository.deleteByPetId(petId);
        petWeightRecordRepository.deleteByPetId(petId);
        petFeedingRecordRepository.deleteByPetId(petId);
        petRepository.delete(pet);
    }

    @Transactional(readOnly = true)
    public List<PetFeedingResponse> listFeedings(UUID userId, UUID householdId, UUID petId) {
        householdService.requireMembership(userId, householdId);
        requirePet(householdId, petId);

        return petFeedingRecordRepository.findTop100ByPetIdOrderByFedAtDesc(petId)
            .stream()
            .map(this::toFeedingResponse)
            .toList();
    }

    @Transactional
    public PetFeedingResponse addFeeding(UUID userId, UUID householdId, UUID petId, PetFeedingRequest request) {
        householdService.requireMembership(userId, householdId);
        Pet pet = requirePet(householdId, petId);

        PetFeedingRecord feeding = new PetFeedingRecord();
        feeding.setPet(pet);
        feeding.setFoodType(request.foodType().trim());
        feeding.setQuantity(request.quantity());
        feeding.setUnit(request.unit().trim().toUpperCase(Locale.ROOT));
        feeding.setFedAt(request.fedAt() == null ? OffsetDateTime.now() : request.fedAt());
        feeding.setNotes(cleanNullable(request.notes()));
        feeding.setAddedByUserId(userId);
        petFeedingRecordRepository.save(feeding);

        if (pet.getFoodStockQuantity() != null
            && pet.getFoodUnit() != null
            && pet.getFoodUnit().equalsIgnoreCase(feeding.getUnit())) {
            pet.setFoodStockQuantity(Math.max(0.0, pet.getFoodStockQuantity() - feeding.getQuantity()));
            petRepository.save(pet);
            evaluatePetFoodAndNotify(pet);
        }

        return toFeedingResponse(feeding);
    }

    @Transactional(readOnly = true)
    public List<PetWeightResponse> listWeights(UUID userId, UUID householdId, UUID petId) {
        householdService.requireMembership(userId, householdId);
        requirePet(householdId, petId);

        return petWeightRecordRepository.findTop100ByPetIdOrderByRecordedAtAsc(petId)
            .stream()
            .map(this::toWeightResponse)
            .toList();
    }

    @Transactional
    public PetWeightResponse addWeight(UUID userId, UUID householdId, UUID petId, PetWeightRequest request) {
        householdService.requireMembership(userId, householdId);
        Pet pet = requirePet(householdId, petId);

        PetWeightRecord weightRecord = new PetWeightRecord();
        weightRecord.setPet(pet);
        weightRecord.setWeightKg(request.weightKg());
        weightRecord.setRecordedAt(request.recordedAt() == null ? OffsetDateTime.now() : request.recordedAt());
        weightRecord.setAddedByUserId(userId);
        petWeightRecordRepository.save(weightRecord);

        pet.setCurrentWeightKg(request.weightKg());
        petRepository.save(pet);

        return toWeightResponse(weightRecord);
    }

    @Transactional(readOnly = true)
    public List<PetCareTaskResponse> listCareTasks(UUID userId, UUID householdId, UUID petId) {
        householdService.requireMembership(userId, householdId);
        requirePet(householdId, petId);

        return petCareTaskRepository.findByPetIdOrderByNextDueAtAsc(petId)
            .stream()
            .map(this::toCareTaskResponse)
            .toList();
    }

    @Transactional
    public PetCareTaskResponse createCareTask(UUID userId, UUID householdId, UUID petId, PetCareTaskRequest request) {
        householdService.requireMembership(userId, householdId);
        Pet pet = requirePet(householdId, petId);

        PetCareTask task = new PetCareTask();
        task.setPet(pet);
        task.setCreatedByUserId(userId);
        applyCareTask(task, request);
        petCareTaskRepository.save(task);

        return toCareTaskResponse(task);
    }

    @Transactional
    public PetCareTaskResponse updateCareTask(
        UUID userId,
        UUID householdId,
        UUID petId,
        UUID taskId,
        PetCareTaskRequest request
    ) {
        householdService.requireMembership(userId, householdId);
        requirePet(householdId, petId);

        PetCareTask task = petCareTaskRepository.findByIdAndPetId(taskId, petId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuidado de mascota no encontrado"));

        applyCareTask(task, request);
        petCareTaskRepository.save(task);

        return toCareTaskResponse(task);
    }

    @Transactional
    public PetCareTaskResponse completeCareTask(
        UUID userId,
        UUID householdId,
        UUID petId,
        UUID taskId,
        OffsetDateTime performedAt
    ) {
        householdService.requireMembership(userId, householdId);
        requirePet(householdId, petId);

        PetCareTask task = petCareTaskRepository.findByIdAndPetId(taskId, petId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuidado de mascota no encontrado"));

        OffsetDateTime lastPerformed = performedAt == null ? OffsetDateTime.now() : performedAt;
        task.setLastPerformedAt(lastPerformed);
        task.setNextDueAt(lastPerformed.plusDays(task.getFrequencyDays()));
        task.setActive(true);
        petCareTaskRepository.save(task);

        return toCareTaskResponse(task);
    }

    @Transactional
    public void scanAndNotifyReminders() {
        petRepository.findByActiveTrue().forEach(this::evaluatePetFoodAndNotify);

        OffsetDateTime now = OffsetDateTime.now();
        List<PetCareTask> careTasks = petCareTaskRepository.findByActiveTrueAndNextDueAtLessThanEqual(now.plusDays(30));
        for (PetCareTask task : careTasks) {
            if (!isTaskDueSoon(task, now)) {
                continue;
            }

            Pet pet = task.getPet();
            UUID householdId = pet.getHousehold().getId();
            String title = "Cuidado mascota proximo: " + pet.getName();
            String body = describeCareTask(task);

            notifyHousehold(
                householdId,
                NotificationType.PET_CARE_DUE,
                title,
                body,
                "PET_CARE_TASK",
                task.getId()
            );
        }
    }

    private void applyPet(Pet pet, PetRequest request) {
        pet.setName(request.name().trim());
        pet.setType(request.type().trim().toUpperCase(Locale.ROOT));
        pet.setChipCode(cleanNullable(request.chipCode()));
        pet.setVeterinarian(cleanNullable(request.veterinarian()));
        pet.setPhotoUrl(cleanNullable(request.photoUrl()));
        pet.setCurrentWeightKg(request.currentWeightKg());
        pet.setFoodName(cleanNullable(request.foodName()));
        pet.setFoodStockQuantity(request.foodStockQuantity());
        pet.setFoodDailyConsumptionQuantity(request.foodDailyConsumptionQuantity());
        pet.setFoodUnit(normalizeNullableUpper(request.foodUnit()));
        pet.setActive(request.active());
    }

    private void applyCareTask(PetCareTask task, PetCareTaskRequest request) {
        int frequencyDays = request.frequencyDays();
        int notifyDaysBefore = request.notifyDaysBefore() == null ? 2 : request.notifyDaysBefore();
        OffsetDateTime lastPerformed = request.lastPerformedAt() == null ? OffsetDateTime.now() : request.lastPerformedAt();

        task.setCareType(request.careType() == null ? PetCareType.OTHER : request.careType());
        task.setDescription(cleanNullable(request.description()));
        task.setFrequencyDays(frequencyDays);
        task.setNotifyDaysBefore(notifyDaysBefore);
        task.setLastPerformedAt(lastPerformed);
        task.setNextDueAt(lastPerformed.plusDays(frequencyDays));
        task.setActive(request.active());
    }

    private Pet requirePet(UUID householdId, UUID petId) {
        return petRepository.findByIdAndHouseholdId(petId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
    }

    private void evaluatePetFoodAndNotify(Pet pet) {
        if (!pet.isActive()) {
            return;
        }

        Double daysRemaining = calculateFoodDaysRemaining(pet);
        if (daysRemaining == null || daysRemaining > FOOD_LOW_THRESHOLD_DAYS) {
            return;
        }

        UUID householdId = pet.getHousehold().getId();
        String foodName = pet.getFoodName() == null ? "Comida " + pet.getName() : pet.getFoodName();
        String unit = pet.getFoodUnit() == null ? "KG" : pet.getFoodUnit();
        double quantityToBuy = recommendedFoodQuantityToBuy(pet);

        shoppingListService.addOrIncreaseForPetFood(
            householdId,
            pet.getCreatedByUserId(),
            foodName,
            quantityToBuy,
            unit
        );

        String title = "Comida mascota baja: " + pet.getName();
        String body = "Quedan " + formatNumber(daysRemaining) + " dias aprox. de " + foodName + ".";

        notifyHousehold(
            householdId,
            NotificationType.PET_FOOD_LOW,
            title,
            body,
            "PET",
            pet.getId()
        );
    }

    private void notifyHousehold(
        UUID householdId,
        NotificationType type,
        String title,
        String body,
        String sourceType,
        UUID sourceEntityId
    ) {
        householdService.householdUserIds(householdId).forEach(userId ->
            notificationService.createIfMissing(
                householdId,
                userId,
                type,
                title,
                body,
                sourceType,
                sourceEntityId
            )
        );
    }

    private PetResponse toPetResponse(Pet pet) {
        Double daysRemaining = calculateFoodDaysRemaining(pet);
        boolean foodLow = daysRemaining != null && daysRemaining <= FOOD_LOW_THRESHOLD_DAYS;

        return new PetResponse(
            pet.getId(),
            pet.getName(),
            pet.getType(),
            pet.getChipCode(),
            pet.getVeterinarian(),
            pet.getPhotoUrl(),
            pet.getCurrentWeightKg(),
            pet.getFoodName(),
            pet.getFoodStockQuantity(),
            pet.getFoodDailyConsumptionQuantity(),
            pet.getFoodUnit(),
            daysRemaining,
            foodLow,
            pet.isActive()
        );
    }

    private PetFeedingResponse toFeedingResponse(PetFeedingRecord feeding) {
        return new PetFeedingResponse(
            feeding.getId(),
            feeding.getFoodType(),
            feeding.getQuantity(),
            feeding.getUnit(),
            feeding.getFedAt(),
            feeding.getNotes(),
            feeding.getAddedByUserId()
        );
    }

    private PetWeightResponse toWeightResponse(PetWeightRecord weightRecord) {
        return new PetWeightResponse(
            weightRecord.getId(),
            weightRecord.getWeightKg(),
            weightRecord.getRecordedAt(),
            weightRecord.getAddedByUserId()
        );
    }

    private PetCareTaskResponse toCareTaskResponse(PetCareTask task) {
        OffsetDateTime now = OffsetDateTime.now();
        return new PetCareTaskResponse(
            task.getId(),
            task.getCareType(),
            task.getDescription(),
            task.getFrequencyDays(),
            task.getNotifyDaysBefore(),
            task.getLastPerformedAt(),
            task.getNextDueAt(),
            isTaskDueSoon(task, now),
            task.isActive()
        );
    }

    private boolean isTaskDueSoon(PetCareTask task, OffsetDateTime now) {
        return task.isActive() && !task.getNextDueAt().isAfter(now.plusDays(task.getNotifyDaysBefore()));
    }

    private Double calculateFoodDaysRemaining(Pet pet) {
        if (pet.getFoodStockQuantity() == null || pet.getFoodDailyConsumptionQuantity() == null) {
            return null;
        }
        if (pet.getFoodDailyConsumptionQuantity() <= 0.0) {
            return null;
        }
        return pet.getFoodStockQuantity() / pet.getFoodDailyConsumptionQuantity();
    }

    private double recommendedFoodQuantityToBuy(Pet pet) {
        if (pet.getFoodDailyConsumptionQuantity() == null || pet.getFoodDailyConsumptionQuantity() <= 0.0) {
            return 1.0;
        }

        double targetStock = pet.getFoodDailyConsumptionQuantity() * 14.0;
        double currentStock = pet.getFoodStockQuantity() == null ? 0.0 : pet.getFoodStockQuantity();
        double needed = Math.max(0.0, targetStock - currentStock);
        return needed <= 0.0 ? pet.getFoodDailyConsumptionQuantity() * 7.0 : needed;
    }

    private String describeCareTask(PetCareTask task) {
        return "Tipo: " + task.getCareType() + ", proxima fecha: " + task.getNextDueAt().toLocalDate() + ".";
    }

    private String cleanNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeNullableUpper(String value) {
        String cleaned = cleanNullable(value);
        return cleaned == null ? null : cleaned.toUpperCase(Locale.ROOT);
    }

    private String formatNumber(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
