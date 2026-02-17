package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.household.HouseholdMember;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {

    List<HouseholdMember> findByUserIdOrderByCreatedAtAsc(UUID userId);

    List<HouseholdMember> findByHouseholdId(UUID householdId);

    Optional<HouseholdMember> findByHouseholdIdAndUserId(UUID householdId, UUID userId);

    boolean existsByHouseholdIdAndUserId(UUID householdId, UUID userId);
}
