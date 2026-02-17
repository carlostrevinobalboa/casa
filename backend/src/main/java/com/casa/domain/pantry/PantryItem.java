package com.casa.domain.pantry;

import java.time.LocalDate;
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
@Table(name = "pantry_item")
public class PantryItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false, length = 120)
    private String productName;

    @Column(nullable = false)
    private double currentQuantity;

    @Column(nullable = false)
    private double minimumQuantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column
    private LocalDate expirationDate;

    @Column(nullable = false, length = 60)
    private String category = "GENERAL";
}
