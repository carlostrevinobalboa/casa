package com.casa.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.integrations.GoogleCalendarLink;

public interface GoogleCalendarLinkRepository extends JpaRepository<GoogleCalendarLink, UUID> {
    Optional<GoogleCalendarLink> findByUserId(UUID userId);
}
