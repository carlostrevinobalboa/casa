package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.catalog.CatalogUnit;

public interface CatalogUnitRepository extends JpaRepository<CatalogUnit, UUID> {

    List<CatalogUnit> findByHouseholdIdOrderByLabelAsc(UUID householdId);

    Optional<CatalogUnit> findByIdAndHouseholdId(UUID id, UUID householdId);

    boolean existsByHouseholdIdAndCodeIgnoreCase(UUID householdId, String code);

    Optional<CatalogUnit> findByHouseholdIdAndCodeIgnoreCase(UUID householdId, String code);
}
