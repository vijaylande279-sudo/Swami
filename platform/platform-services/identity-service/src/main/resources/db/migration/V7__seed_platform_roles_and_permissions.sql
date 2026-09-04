-- Seeds the baseline permission catalog and the two PLATFORM-scoped system roles
-- (which are singleton, tenant_id IS NULL rows shared across the platform - safe
-- because platform admins aren't tenant-scoped). TENANT_ADMIN/TENANT_MANAGER are
-- deliberately NOT seeded here: each tenant gets its own row (tenant_id = that
-- tenant's id) created programmatically by RoleTemplateService at signup, so one
-- tenant editing "their" TENANT_ADMIN role's permissions can never affect another
-- tenant sharing the same role name.

INSERT INTO permissions (code, description) VALUES
    ('platform:tenant:read', 'View any tenant''s profile and status'),
    ('platform:tenant:suspend', 'Suspend a tenant'),
    ('platform:tenant:status:update', 'Manually override a tenant''s lifecycle status'),
    ('tenant:profile:read', 'View own tenant profile'),
    ('tenant:profile:update', 'Update own tenant profile'),
    ('tenant:employee:invite', 'Invite an employee to the tenant'),
    ('tenant:employee:read', 'List the tenant''s employees'),
    ('tenant:employee:revoke', 'Revoke a pending employee invite'),
    ('tenant:role:create', 'Create a custom role for the tenant'),
    ('tenant:role:manage', 'Edit permissions on a custom role'),
    ('tenant:role:read', 'List the tenant''s roles');

INSERT INTO roles (tenant_id, name, scope, is_system) VALUES
    (NULL, 'PLATFORM_SUPER_ADMIN', 'PLATFORM', TRUE),
    (NULL, 'PLATFORM_SUPPORT', 'PLATFORM', TRUE);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'PLATFORM_SUPER_ADMIN'
  AND p.code IN ('platform:tenant:read', 'platform:tenant:suspend', 'platform:tenant:status:update');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'PLATFORM_SUPPORT'
  AND p.code IN ('platform:tenant:read');
