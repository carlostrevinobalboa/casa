package com.casa.domain.activities;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.casa.domain.common.BaseEntity;
import com.casa.domain.household.Household;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "activity_session")
public class ActivitySession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @Column(nullable = false)
    private UUID performedByUserId;

    @Column
    private UUID petId;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    @Column(nullable = false)
    private OffsetDateTime endedAt;

    @Column(nullable = false)
    private long durationSeconds;

    @Column(nullable = false)
    private double distanceKm;

    @Column(length = 120)
    private String title;

    @Column(length = 600)
    private String notes;

    @Column(nullable = false)
    private boolean gpsTracked;
}
