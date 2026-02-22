package com.casa.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.calendar.CalendarEvent;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {
    List<CalendarEvent> findByHouseholdIdOrderByStartAtAsc(UUID householdId);
    List<CalendarEvent> findByHouseholdIdAndCreatedByUserIdOrderByStartAtAsc(UUID householdId, UUID createdByUserId);
}
