package com.casa.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.casa.domain.activities.ActivityPoint;

public interface ActivityPointRepository extends JpaRepository<ActivityPoint, UUID> {

    List<ActivityPoint> findByActivityIdOrderBySequenceNumberAsc(UUID activityId);

    void deleteByActivityId(UUID activityId);
}
