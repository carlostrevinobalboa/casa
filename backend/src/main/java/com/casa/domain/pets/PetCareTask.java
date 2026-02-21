package com.casa.domain.pets;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.casa.domain.common.BaseEntity;
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
@Table(name = "pet_care_task")
public class PetCareTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PetCareType careType;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private int frequencyDays;

    @Column(nullable = false)
    private int notifyDaysBefore = 2;

    @Column
    private OffsetDateTime lastPerformedAt;

    @Column(nullable = false)
    private OffsetDateTime nextDueAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private UUID createdByUserId;
}
