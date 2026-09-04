-- Doc §15.7: "Only TENANT_ADMIN with platform:billing:purchase can start a checkout."
-- Seeded directly onto the TENANT_ADMIN role only - RoleService's custom role
-- builder filters every platform:* permission out of GET /permissions, so this one
-- can never be delegated to an arbitrary employee role via that path.
INSERT INTO permissions (code, description) VALUES
    ('platform:billing:purchase', 'Start a checkout for the tenant''s own subscription');

-- Grant it to every existing TENANT_ADMIN role (both the seed template and any
-- already-provisioned per-tenant rows), so tenants created before this migration
-- don't need to be recreated to get billing access.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'TENANT_ADMIN' AND p.code = 'platform:billing:purchase'
ON CONFLICT DO NOTHING;
