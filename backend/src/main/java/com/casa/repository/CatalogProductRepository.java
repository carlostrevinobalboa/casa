package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.catalog.CatalogProduct;

public interface CatalogProductRepository extends JpaRepository<CatalogProduct, UUID> {

    List<CatalogProduct> findByHouseholdIdOrderByNameAsc(UUID householdId);

    Optional<CatalogProduct> findByIdAndHouseholdId(UUID id, UUID householdId);

    boolean existsByHouseholdIdAndNameIgnoreCase(UUID householdId, String name);

    Optional<CatalogProduct> findByHouseholdIdAndNameIgnoreCase(UUID householdId, String name);
}
