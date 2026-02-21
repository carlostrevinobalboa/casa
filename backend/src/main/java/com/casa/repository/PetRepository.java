package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.pets.Pet;

public interface PetRepository extends JpaRepository<Pet, UUID> {

    List<Pet> findByHouseholdIdOrderByNameAsc(UUID householdId);

    Optional<Pet> findByIdAndHouseholdId(UUID id, UUID householdId);

    List<Pet> findByActiveTrue();
}
