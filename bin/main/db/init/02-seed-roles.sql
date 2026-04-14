INSERT INTO role (role_id, role_code, description, created_at, updated_at)
VALUES
    ('9de6fc0e-13d2-42a1-9232-4df6cdbf6f31', 'ORG_OWNER', 'Organization owner role', NOW(), NOW()),
    ('40f2f8c9-d79e-4431-9f53-f9db3c14c885', 'ORG_ADMIN', 'Organization administrator role', NOW(), NOW()),
    ('ba5992e7-eae0-4f40-ab7f-e43f1f7148a5', 'ORG_MANAGER', 'Organization manager role', NOW(), NOW()),
    ('53d95712-24b2-448d-b985-f051f8f0b65f', 'ORG_USER', 'Organization user role', NOW(), NOW()),
    ('26ec9f4a-ca7d-4192-bf13-fdb8c4fbd37e', 'ORG_READONLY', 'Organization readonly role', NOW(), NOW()),
    ('3f4b2c76-8be7-4ea2-b620-4a8c6d29d30f', 'ARKA_ADMIN', 'Global platform administrator role', NOW(), NOW())
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'ORGANIZATION', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.user.create', 'iam.user', 'create'),
    ('iam.user.read', 'iam.user', 'read'),
    ('iam.user.update', 'iam.user', 'update'),
    ('iam.user.block', 'iam.user', 'block'),
    ('iam.user.unblock', 'iam.user', 'unblock'),
    ('iam.user.assign-role', 'iam.user', 'assign-role'),
    ('iam.user.revoke-session', 'iam.user', 'revoke-session'),
    ('iam.session.read', 'iam.session', 'read'),
    ('iam.session.revoke', 'iam.session', 'revoke'),
    ('iam.permission.read', 'iam.permission', 'read'),
    ('directory.organization.read', 'directory.organization', 'read'),
    ('directory.organization.update', 'directory.organization', 'update'),
    ('directory.profile.read', 'directory.profile', 'read'),
    ('directory.profile.create', 'directory.profile', 'create'),
    ('directory.profile.update', 'directory.profile', 'update')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code IN ('ORG_OWNER', 'ARKA_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'ORGANIZATION', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.user.create', 'iam.user', 'create'),
    ('iam.user.read', 'iam.user', 'read'),
    ('iam.user.update', 'iam.user', 'update'),
    ('iam.user.assign-role', 'iam.user', 'assign-role'),
    ('iam.user.revoke-session', 'iam.user', 'revoke-session'),
    ('iam.session.read', 'iam.session', 'read'),
    ('iam.session.revoke', 'iam.session', 'revoke'),
    ('iam.permission.read', 'iam.permission', 'read'),
    ('directory.organization.read', 'directory.organization', 'read'),
    ('directory.profile.read', 'directory.profile', 'read'),
    ('directory.profile.create', 'directory.profile', 'create'),
    ('directory.profile.update', 'directory.profile', 'update')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'ORG_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'ORGANIZATION', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.user.read', 'iam.user', 'read'),
    ('iam.session.read', 'iam.session', 'read'),
    ('iam.permission.read', 'iam.permission', 'read'),
    ('directory.organization.read', 'directory.organization', 'read'),
    ('directory.profile.read', 'directory.profile', 'read')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'ORG_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'ORGANIZATION', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.permission.read', 'iam.permission', 'read'),
    ('directory.profile.read', 'directory.profile', 'read')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'ORG_USER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'ORGANIZATION', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.permission.read', 'iam.permission', 'read'),
    ('directory.organization.read', 'directory.organization', 'read'),
    ('directory.profile.read', 'directory.profile', 'read')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'ORG_READONLY'
ON CONFLICT DO NOTHING;

INSERT INTO role_assignment_policy (assigner_role_id, assignable_role_id, created_at)
SELECT assigner.role_id, assignable.role_id, NOW()
FROM role assigner
JOIN role assignable ON TRUE
JOIN (
    VALUES
    ('ARKA_ADMIN', 'ORG_OWNER'),
    ('ARKA_ADMIN', 'ORG_ADMIN'),
    ('ARKA_ADMIN', 'ORG_MANAGER'),
    ('ARKA_ADMIN', 'ORG_USER'),
    ('ARKA_ADMIN', 'ORG_READONLY'),
    ('ORG_OWNER', 'ORG_ADMIN'),
    ('ORG_OWNER', 'ORG_MANAGER'),
    ('ORG_OWNER', 'ORG_USER'),
    ('ORG_OWNER', 'ORG_READONLY'),
    ('ORG_ADMIN', 'ORG_MANAGER'),
    ('ORG_ADMIN', 'ORG_USER'),
    ('ORG_ADMIN', 'ORG_READONLY')
) AS policy(assigner_code, assignable_code)
    ON policy.assigner_code = assigner.role_code
   AND policy.assignable_code = assignable.role_code
ON CONFLICT DO NOTHING;
