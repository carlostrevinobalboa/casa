package com.casa.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.pets.PetWeightRecord;

public interface PetWeightRecordRepository extends JpaRepository<PetWeightRecord, UUID> {

    List<PetWeightRecord> findTop100ByPetIdOrderByRecordedAtAsc(UUID petId);

    void deleteByPetId(UUID petId);
}
