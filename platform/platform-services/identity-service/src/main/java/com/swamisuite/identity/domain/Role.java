package com.swamisuite.identity.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    /** Null for platform-scoped roles (PLATFORM_SUPER_ADMIN, PLATFORM_SUPPORT). */
    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleScope scope;

    /** System roles (TENANT_ADMIN, TENANT_MANAGER, PLATFORM_*) can't be deleted. */
    @Column(name = "is_system", nullable = false)
    private boolean system = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    public Role(UUID tenantId, String name, RoleScope scope, boolean system) {
        this.tenantId = tenantId;
        this.name = name;
        this.scope = scope;
        this.system = system;
    }

    public enum RoleScope {
        PLATFORM, TENANT
    }
}
