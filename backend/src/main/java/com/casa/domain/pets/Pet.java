package com.casa.domain.pets;

import java.util.UUID;
import com.casa.domain.common.BaseEntity;
import com.casa.domain.household.Household;
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
@Table(name = "pet")
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(length = 80)
    private String chipCode;

    @Column(length = 120)
    private String veterinarian;

    @Column(length = 400)
    private String photoUrl;

    @Column
    private Double currentWeightKg;

    @Column(length = 120)
    private String foodName;

    @Column
    private Double foodStockQuantity;

    @Column
    private Double foodDailyConsumptionQuantity;

    @Column(length = 20)
    private String foodUnit;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private UUID createdByUserId;
}
