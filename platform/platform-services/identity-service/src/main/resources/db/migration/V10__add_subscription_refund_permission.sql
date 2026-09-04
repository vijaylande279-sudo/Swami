-- Doc §4.2 names platform:subscription:refund explicitly as an example
-- PLATFORM_SUPER_ADMIN permission - payment-service's refund endpoint requires it.
INSERT INTO permissions (code, description) VALUES
    ('platform:subscription:refund', 'Issue a refund for any tenant''s payment');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'PLATFORM_SUPER_ADMIN' AND p.code = 'platform:subscription:refund'
ON CONFLICT DO NOTHING;
