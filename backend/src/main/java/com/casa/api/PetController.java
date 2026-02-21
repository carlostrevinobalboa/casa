package com.casa.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.pets.PetCareCompleteRequest;
import com.casa.api.dto.pets.PetCareTaskRequest;
import com.casa.api.dto.pets.PetCareTaskResponse;
import com.casa.api.dto.pets.PetFeedingRequest;
import com.casa.api.dto.pets.PetFeedingResponse;
import com.casa.api.dto.pets.PetRequest;
import com.casa.api.dto.pets.PetResponse;
import com.casa.api.dto.pets.PetWeightRequest;
import com.casa.api.dto.pets.PetWeightResponse;
import com.casa.security.AppUserPrincipal;
import com.casa.service.PetService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/households/{householdId}/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public List<PetResponse> list(Authentication authentication, @PathVariable UUID householdId) {
        return petService.listPets(currentUserId(authentication), householdId);
    }

    @PostMapping
    public PetResponse create(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody PetRequest request
    ) {
        return petService.createPet(currentUserId(authentication), householdId, request);
    }

    @PutMapping("/{petId}")
    public PetResponse update(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId,
        @Valid @RequestBody PetRequest request
    ) {
        return petService.updatePet(currentUserId(authentication), householdId, petId, request);
    }

    @DeleteMapping("/{petId}")
    public void delete(Authentication authentication, @PathVariable UUID householdId, @PathVariable UUID petId) {
        petService.deletePet(currentUserId(authentication), householdId, petId);
    }

    @GetMapping("/{petId}/feedings")
    public List<PetFeedingResponse> listFeedings(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId
    ) {
        return petService.listFeedings(currentUserId(authentication), householdId, petId);
    }

    @PostMapping("/{petId}/feedings")
    public PetFeedingResponse addFeeding(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId,
        @Valid @RequestBody PetFeedingRequest request
    ) {
        return petService.addFeeding(currentUserId(authentication), householdId, petId, request);
    }

    @GetMapping("/{petId}/weights")
    public List<PetWeightResponse> listWeights(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId
    ) {
        return petService.listWeights(currentUserId(authentication), householdId, petId);
    }

    @PostMapping("/{petId}/weights")
    public PetWeightResponse addWeight(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId,
        @Valid @RequestBody PetWeightRequest request
    ) {
        return petService.addWeight(currentUserId(authentication), householdId, petId, request);
    }

    @GetMapping("/{petId}/care-tasks")
    public List<PetCareTaskResponse> listCareTasks(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId
    ) {
        return petService.listCareTasks(currentUserId(authentication), householdId, petId);
    }

    @PostMapping("/{petId}/care-tasks")
    public PetCareTaskResponse createCareTask(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId,
        @Valid @RequestBody PetCareTaskRequest request
    ) {
        return petService.createCareTask(currentUserId(authentication), householdId, petId, request);
    }

    @PutMapping("/{petId}/care-tasks/{taskId}")
    public PetCareTaskResponse updateCareTask(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId,
        @PathVariable UUID taskId,
        @Valid @RequestBody PetCareTaskRequest request
    ) {
        return petService.updateCareTask(currentUserId(authentication), householdId, petId, taskId, request);
    }

    @PostMapping("/{petId}/care-tasks/{taskId}/complete")
    public PetCareTaskResponse completeCareTask(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID petId,
        @PathVariable UUID taskId,
        @RequestBody(required = false) PetCareCompleteRequest request
    ) {
        return petService.completeCareTask(
            currentUserId(authentication),
            householdId,
            petId,
            taskId,
            request == null ? null : request.performedAt()
        );
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
