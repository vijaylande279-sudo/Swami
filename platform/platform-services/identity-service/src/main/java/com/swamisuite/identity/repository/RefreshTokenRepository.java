package com.swamisuite.identity.repository;

import com.swamisuite.identity.domain.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    java.util.List<RefreshToken> findByFamilyId(UUID familyId);
}
