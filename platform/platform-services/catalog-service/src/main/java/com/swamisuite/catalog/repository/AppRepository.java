package com.swamisuite.catalog.repository;

import com.swamisuite.catalog.domain.App;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRepository extends JpaRepository<App, UUID> {
    Optional<App> findByKey(String key);

    List<App> findByActiveTrue();
}
