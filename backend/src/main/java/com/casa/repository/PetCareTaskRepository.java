package com.casa.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.pets.PetCareTask;

public interface PetCareTaskRepository extends JpaRepository<PetCareTask, UUID> {

    List<PetCareTask> findByPetIdOrderByNextDueAtAsc(UUID petId);

    Optional<PetCareTask> findByIdAndPetId(UUID id, UUID petId);

    List<PetCareTask> findByActiveTrueAndNextDueAtLessThanEqual(OffsetDateTime threshold);

    void deleteByPetId(UUID petId);
}
