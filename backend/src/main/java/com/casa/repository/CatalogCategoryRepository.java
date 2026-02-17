package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.catalog.CatalogCategory;

public interface CatalogCategoryRepository extends JpaRepository<CatalogCategory, UUID> {

    List<CatalogCategory> findByHouseholdIdOrderByNameAsc(UUID householdId);

    Optional<CatalogCategory> findByIdAndHouseholdId(UUID id, UUID householdId);

    boolean existsByHouseholdIdAndNameIgnoreCase(UUID householdId, String name);

    Optional<CatalogCategory> findByHouseholdIdAndNameIgnoreCase(UUID householdId, String name);
}
