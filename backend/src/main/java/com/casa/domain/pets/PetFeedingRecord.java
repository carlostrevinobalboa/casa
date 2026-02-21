package com.casa.domain.pets;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.casa.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "pet_feeding_record")
public class PetFeedingRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 120)
    private String foodType;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private OffsetDateTime fedAt;

    @Column(length = 300)
    private String notes;

    @Column(nullable = false)
    private UUID addedByUserId;
}
