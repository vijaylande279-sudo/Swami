package com.swamisuite.catalog.repository;

import com.swamisuite.catalog.domain.Plan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    List<Plan> findByTierId(UUID tierId);
}
