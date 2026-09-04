package com.swamisuite.catalog.repository;

import com.swamisuite.catalog.domain.Tier;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TierRepository extends JpaRepository<Tier, UUID> {
    List<Tier> findByAppId(UUID appId);
}
