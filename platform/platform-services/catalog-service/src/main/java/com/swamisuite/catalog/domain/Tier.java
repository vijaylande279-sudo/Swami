package com.swamisuite.catalog.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** Standard/Large per doc §1's pricing table - one tier per app in practice today, but modeled separately for future apps that may offer more than one. */
@Entity
@Table(name = "tiers")
@Getter
@Setter
@NoArgsConstructor
public class Tier {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "app_id", nullable = false, columnDefinition = "uuid")
    private UUID appId;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public Tier(UUID appId, String key, String name, int sortOrder) {
        this.appId = appId;
        this.key = key;
        this.name = name;
        this.sortOrder = sortOrder;
    }
}
