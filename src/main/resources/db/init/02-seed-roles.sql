INSERT INTO role (role_id, role_code, description, status, protected_role, exclusive_assignment, created_at, updated_at)
VALUES
    ('9de6fc0e-13d2-42a1-9232-4df6cdbf6f31', 'SYSTEM_ADMIN', 'System administrator role', 'ACTIVE', TRUE, TRUE, NOW(), NOW()),
    ('40f2f8c9-d79e-4431-9f53-f9db3c14c885', 'ACCESS_ADMIN', 'Access administrator role', 'ACTIVE', FALSE, FALSE, NOW(), NOW()),
    ('ba5992e7-eae0-4f40-ab7f-e43f1f7148a5', 'ACCESS_MANAGER', 'Access manager role', 'ACTIVE', FALSE, FALSE, NOW(), NOW()),
    ('53d95712-24b2-448d-b985-f051f8f0b65f', 'ACCOUNT_USER', 'Standard account role', 'ACTIVE', FALSE, FALSE, NOW(), NOW()),
    ('26ec9f4a-ca7d-4192-bf13-fdb8c4fbd37e', 'ACCOUNT_READONLY', 'Read-only account role', 'ACTIVE', FALSE, FALSE, NOW(), NOW())
ON CONFLICT (role_code) DO NOTHING;

UPDATE role
SET protected_role = TRUE,
    exclusive_assignment = TRUE,
    updated_at = NOW()
WHERE role_code = 'SYSTEM_ADMIN';

INSERT INTO permission (permission_id, permission_code, resource, action, scope, description, status, system_permission, created_at, updated_at)
VALUES
    ('c3b082e6-0345-5f54-afee-f9ea8ca158e8', 'iam.account.create', 'iam.account', 'create', 'GLOBAL', 'iam account create', 'ACTIVE', TRUE, NOW(), NOW()),
    ('0d59cb96-3f02-5a96-a7a1-c9da67b48089', 'iam.account.read', 'iam.account', 'read', 'GLOBAL', 'iam account read', 'ACTIVE', TRUE, NOW(), NOW()),
    ('c4fc4625-835f-543a-b1a4-cb70e86f0ebe', 'iam.account.update', 'iam.account', 'update', 'GLOBAL', 'iam account update', 'ACTIVE', TRUE, NOW(), NOW()),
    ('61f629cf-cfcd-57c9-8500-70a3b4221055', 'iam.account.block', 'iam.account', 'block', 'GLOBAL', 'iam account block', 'ACTIVE', TRUE, NOW(), NOW()),
    ('d690f170-d715-537b-a863-9aabd8e0acc6', 'iam.account.unblock', 'iam.account', 'unblock', 'GLOBAL', 'iam account unblock', 'ACTIVE', TRUE, NOW(), NOW()),
    ('a42ec4fb-8cb5-565c-bb2a-6170b5bd2976', 'iam.access.assign-role', 'iam.access', 'assign-role', 'GLOBAL', 'iam access assign role', 'ACTIVE', TRUE, NOW(), NOW()),
    ('be955678-4d81-522a-84f8-da253c7ed4e7', 'iam.access.revoke-role', 'iam.access', 'revoke-role', 'GLOBAL', 'iam access revoke role', 'ACTIVE', TRUE, NOW(), NOW()),
    ('f3f2ae18-f415-59c1-91d8-8cd848f24964', 'iam.session.read', 'iam.session', 'read', 'GLOBAL', 'iam session read', 'ACTIVE', TRUE, NOW(), NOW()),
    ('6b3a58ac-5cc9-5188-a46c-1997ffa0254d', 'iam.session.revoke', 'iam.session', 'revoke', 'GLOBAL', 'iam session revoke', 'ACTIVE', TRUE, NOW(), NOW()),
    ('d6db73c2-1e0b-516b-8ea1-98f93fbf9130', 'iam.permission.read', 'iam.permission', 'read', 'GLOBAL', 'iam permission read', 'ACTIVE', TRUE, NOW(), NOW()),
    ('c6620222-ca6d-5cf0-b59b-f5d24ce671f0', 'iam.role.create', 'iam.role', 'create', 'GLOBAL', 'iam role create', 'ACTIVE', TRUE, NOW(), NOW()),
    ('3cecee4e-09c6-5f6a-bb95-33d864fe2fef', 'iam.role.read', 'iam.role', 'read', 'GLOBAL', 'iam role read', 'ACTIVE', TRUE, NOW(), NOW()),
    ('c0e59714-7b93-50a2-9325-341525078387', 'iam.role.update', 'iam.role', 'update', 'GLOBAL', 'iam role update', 'ACTIVE', TRUE, NOW(), NOW()),
    ('6f0078f3-af67-5c4f-b1ac-48fdb50e087c', 'iam.permission.create', 'iam.permission', 'create', 'GLOBAL', 'iam permission create', 'ACTIVE', TRUE, NOW(), NOW()),
    ('566deadb-805e-5556-9077-3e516c9404da', 'iam.permission.update', 'iam.permission', 'update', 'GLOBAL', 'iam permission update', 'ACTIVE', TRUE, NOW(), NOW()),
    ('d016ef46-2cc9-5aba-b1f5-c38071cbe3f1', 'iam.access-profile.read', 'iam.access-profile', 'read', 'GLOBAL', 'iam access profile read', 'ACTIVE', TRUE, NOW(), NOW())
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'GLOBAL', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.account.create', 'iam.account', 'create'),
    ('iam.account.read', 'iam.account', 'read'),
    ('iam.account.update', 'iam.account', 'update'),
    ('iam.account.block', 'iam.account', 'block'),
    ('iam.account.unblock', 'iam.account', 'unblock'),
    ('iam.access.assign-role', 'iam.access', 'assign-role'),
    ('iam.access.revoke-role', 'iam.access', 'revoke-role'),
    ('iam.session.read', 'iam.session', 'read'),
    ('iam.session.revoke', 'iam.session', 'revoke'),
    ('iam.permission.read', 'iam.permission', 'read'),
    ('iam.role.create', 'iam.role', 'create'),
    ('iam.role.read', 'iam.role', 'read'),
    ('iam.role.update', 'iam.role', 'update'),
    ('iam.permission.create', 'iam.permission', 'create'),
    ('iam.permission.update', 'iam.permission', 'update'),
    ('iam.access-profile.read', 'iam.access-profile', 'read')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'SYSTEM_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'GLOBAL', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.account.create', 'iam.account', 'create'),
    ('iam.account.read', 'iam.account', 'read'),
    ('iam.account.update', 'iam.account', 'update'),
    ('iam.account.block', 'iam.account', 'block'),
    ('iam.account.unblock', 'iam.account', 'unblock'),
    ('iam.access.assign-role', 'iam.access', 'assign-role'),
    ('iam.access.revoke-role', 'iam.access', 'revoke-role'),
    ('iam.session.read', 'iam.session', 'read'),
    ('iam.session.revoke', 'iam.session', 'revoke'),
    ('iam.permission.read', 'iam.permission', 'read'),
    ('iam.role.read', 'iam.role', 'read'),
    ('iam.access-profile.read', 'iam.access-profile', 'read')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'ACCESS_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'GLOBAL', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.account.read', 'iam.account', 'read'),
    ('iam.session.read', 'iam.session', 'read'),
    ('iam.permission.read', 'iam.permission', 'read'),
    ('iam.role.read', 'iam.role', 'read'),
    ('iam.access-profile.read', 'iam.access-profile', 'read')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'ACCESS_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'GLOBAL', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.permission.read', 'iam.permission', 'read'),
    ('iam.access-profile.read', 'iam.access-profile', 'read')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'ACCOUNT_USER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
SELECT r.role_id, p.permission_code, p.resource, p.action, 'GLOBAL', NOW()
FROM role r
JOIN (
    VALUES
    ('iam.account.read', 'iam.account', 'read'),
    ('iam.session.read', 'iam.session', 'read'),
    ('iam.permission.read', 'iam.permission', 'read'),
    ('iam.role.read', 'iam.role', 'read'),
    ('iam.access-profile.read', 'iam.access-profile', 'read')
) AS p(permission_code, resource, action) ON TRUE
WHERE r.role_code = 'ACCOUNT_READONLY'
ON CONFLICT DO NOTHING;

INSERT INTO role_assignment_policy (assigner_role_id, assignable_role_id, created_at)
SELECT assigner.role_id, assignable.role_id, NOW()
FROM role assigner
JOIN role assignable ON TRUE
JOIN (
    VALUES
    ('SYSTEM_ADMIN', 'ACCESS_ADMIN'),
    ('SYSTEM_ADMIN', 'ACCESS_MANAGER'),
    ('SYSTEM_ADMIN', 'ACCOUNT_USER'),
    ('SYSTEM_ADMIN', 'ACCOUNT_READONLY'),
    ('ACCESS_ADMIN', 'ACCESS_MANAGER'),
    ('ACCESS_ADMIN', 'ACCOUNT_USER'),
    ('ACCESS_ADMIN', 'ACCOUNT_READONLY')
) AS policy(assigner_code, assignable_code)
    ON policy.assigner_code = assigner.role_code
   AND policy.assignable_code = assignable.role_code
ON CONFLICT DO NOTHING;
