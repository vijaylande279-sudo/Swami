package com.swamisuite.identity.repository;

import com.swamisuite.identity.domain.Permission;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
}
