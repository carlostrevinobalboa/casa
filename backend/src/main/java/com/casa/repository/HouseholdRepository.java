package com.casa.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.household.Household;

public interface HouseholdRepository extends JpaRepository<Household, UUID> {

    Optional<Household> findByInviteCodeIgnoreCase(String inviteCode);

    boolean existsByInviteCodeIgnoreCase(String inviteCode);
}
