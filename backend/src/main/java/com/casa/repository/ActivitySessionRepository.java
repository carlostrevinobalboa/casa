package com.casa.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.activities.ActivitySession;

public interface ActivitySessionRepository extends JpaRepository<ActivitySession, UUID> {

    List<ActivitySession> findTop200ByHouseholdIdOrderByStartedAtDesc(UUID householdId);

    List<ActivitySession> findTop200ByHouseholdIdAndPerformedByUserIdOrderByStartedAtDesc(UUID householdId, UUID userId);

    List<ActivitySession> findByHouseholdIdAndStartedAtBetweenOrderByStartedAtDesc(
        UUID householdId,
        OffsetDateTime from,
        OffsetDateTime to
    );

    List<ActivitySession> findByHouseholdIdAndPerformedByUserIdAndStartedAtBetweenOrderByStartedAtDesc(
        UUID householdId,
        UUID userId,
        OffsetDateTime from,
        OffsetDateTime to
    );

    long countByHouseholdIdAndPerformedByUserIdAndStartedAtGreaterThanEqual(
        UUID householdId,
        UUID userId,
        OffsetDateTime from
    );
}
