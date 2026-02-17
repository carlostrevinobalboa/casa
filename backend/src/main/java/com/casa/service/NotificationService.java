package com.casa.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.notification.NotificationResponse;
import com.casa.domain.household.Household;
import com.casa.domain.identity.UserAccount;
import com.casa.domain.notifications.Notification;
import com.casa.domain.notifications.NotificationType;
import com.casa.repository.HouseholdRepository;
import com.casa.repository.NotificationRepository;
import com.casa.repository.UserAccountRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final HouseholdRepository householdRepository;
    private final UserAccountRepository userAccountRepository;
    private final HouseholdService householdService;

    public NotificationService(
        NotificationRepository notificationRepository,
        HouseholdRepository householdRepository,
        UserAccountRepository userAccountRepository,
        HouseholdService householdService
    ) {
        this.notificationRepository = notificationRepository;
        this.householdRepository = householdRepository;
        this.userAccountRepository = userAccountRepository;
        this.householdService = householdService;
    }

    @Transactional
    public void createIfMissing(
        UUID householdId,
        UUID recipientUserId,
        NotificationType type,
        String title,
        String body,
        String sourceType,
        UUID sourceEntityId
    ) {
        boolean exists = notificationRepository.existsByRecipientIdAndHouseholdIdAndTypeAndSourceEntityIdAndReadAtIsNull(
            recipientUserId,
            householdId,
            type,
            sourceEntityId
        );

        if (exists) {
            return;
        }

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        UserAccount recipient = userAccountRepository.findById(recipientUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario destinatario no encontrado"));

        Notification notification = new Notification();
        notification.setHousehold(household);
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setScheduledFor(OffsetDateTime.now());
        notification.setSourceType(sourceType);
        notification.setSourceEntityId(sourceEntityId);

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);

        return notificationRepository.findTop30ByHouseholdIdAndRecipientIdOrderByScheduledForDesc(householdId, userId)
            .stream()
            .map(notification -> new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getScheduledFor(),
                notification.getReadAt()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);
        return notificationRepository.countByHouseholdIdAndRecipientIdAndReadAtIsNull(householdId, userId);
    }

    @Transactional
    public void markAsRead(UUID userId, UUID householdId, UUID notificationId) {
        householdService.requireMembership(userId, householdId);

        Notification notification = notificationRepository.findByIdAndHouseholdIdAndRecipientId(notificationId, householdId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificacion no encontrada"));

        if (notification.getReadAt() == null) {
            notification.setReadAt(OffsetDateTime.now());
            notificationRepository.save(notification);
        }
    }
}
