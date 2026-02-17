package com.casa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.notifications.Notification;
import com.casa.domain.notifications.NotificationType;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    boolean existsByRecipientIdAndHouseholdIdAndTypeAndSourceEntityIdAndReadAtIsNull(
        UUID recipientId,
        UUID householdId,
        NotificationType type,
        UUID sourceEntityId
    );

    long countByHouseholdIdAndRecipientIdAndReadAtIsNull(UUID householdId, UUID recipientId);

    List<Notification> findTop30ByHouseholdIdAndRecipientIdOrderByScheduledForDesc(UUID householdId, UUID recipientId);

    Optional<Notification> findByIdAndHouseholdIdAndRecipientId(UUID id, UUID householdId, UUID recipientId);
}
