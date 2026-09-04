package com.swamisuite.catalog.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** One of the four vertical apps (restaurant, coffee-shop, bar-restro, hotel), per doc §1. */
@Entity
@Table(name = "apps")
@Getter
@Setter
@NoArgsConstructor
public class App {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public App(String key, String name, String description) {
        this.key = key;
        this.name = name;
        this.description = description;
    }
}
