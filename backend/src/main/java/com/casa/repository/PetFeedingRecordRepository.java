package com.casa.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.pets.PetFeedingRecord;

public interface PetFeedingRecordRepository extends JpaRepository<PetFeedingRecord, UUID> {

    List<PetFeedingRecord> findTop100ByPetIdOrderByFedAtDesc(UUID petId);

    void deleteByPetId(UUID petId);
}
