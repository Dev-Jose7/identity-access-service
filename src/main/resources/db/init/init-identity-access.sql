-- =========================
-- IDENTITY-ACCESS - RUNTIME SCHEMA
-- Register / Login / Refresh / Logout
-- =========================

BEGIN;

-- -------------------------
-- USER ACCOUNT
-- -------------------------
CREATE TABLE IF NOT EXISTS user_account (
    user_id              VARCHAR(100) PRIMARY KEY,
    email                VARCHAR(320) NOT NULL UNIQUE,
    status               VARCHAR(30)  NOT NULL,
    failed_login_count   INTEGER      NOT NULL DEFAULT 0,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL
);

ALTER TABLE user_account
    ADD COLUMN IF NOT EXISTS failed_login_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE user_account
    DROP COLUMN IF EXISTS organization_id;

CREATE INDEX IF NOT EXISTS idx_user_account_email ON user_account (email);
CREATE INDEX IF NOT EXISTS idx_user_account_status ON user_account (status);

-- -------------------------
-- CREDENTIAL
-- -------------------------
CREATE TABLE IF NOT EXISTS credential (
    credential_id        VARCHAR(100) PRIMARY KEY,
    user_id              VARCHAR(100) NOT NULL,
    credential_type      VARCHAR(30)  NOT NULL,
    credential_purpose   VARCHAR(30)  NOT NULL,
    provider             VARCHAR(30)  NOT NULL,
    status               VARCHAR(30)  NOT NULL,
    last_used_at         TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    CONSTRAINT fk_credential_user
        FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_credential_identity UNIQUE (user_id, credential_type, credential_purpose, provider)
);

CREATE INDEX IF NOT EXISTS idx_credential_user_id ON credential (user_id);
CREATE INDEX IF NOT EXISTS idx_credential_status ON credential (status);

-- -------------------------
-- CREDENTIAL PASSWORD
-- -------------------------
CREATE TABLE IF NOT EXISTS credential_password (
    credential_id        VARCHAR(100) PRIMARY KEY,
    password_hash        TEXT         NOT NULL,
    hash_algorithm       VARCHAR(30)  NOT NULL,
    password_changed_at  TIMESTAMP    NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    CONSTRAINT fk_credential_password_credential
        FOREIGN KEY (credential_id) REFERENCES credential(credential_id)
        ON DELETE CASCADE
);

ALTER TABLE credential_password
    ADD COLUMN IF NOT EXISTS hash_algorithm VARCHAR(30),
    ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_credential_password_updated_at ON credential_password (updated_at);

-- -------------------------
-- ROLE
-- -------------------------
CREATE TABLE IF NOT EXISTS role (
    role_id              VARCHAR(100) PRIMARY KEY,
    role_code            VARCHAR(100) NOT NULL UNIQUE,
    description          VARCHAR(255) NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL
);

-- -------------------------
-- ROLE PERMISSION
-- -------------------------
CREATE TABLE IF NOT EXISTS role_permission (
    role_id              VARCHAR(100) NOT NULL,
    permission_code      VARCHAR(150) NOT NULL,
    resource             VARCHAR(100) NOT NULL,
    action               VARCHAR(100) NOT NULL,
    scope                VARCHAR(50)  NOT NULL DEFAULT 'ORGANIZATION',
    created_at           TIMESTAMP    NOT NULL,
    PRIMARY KEY (role_id, permission_code, resource, action, scope),
    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id) REFERENCES role(role_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS role_assignment_policy (
    assigner_role_id     VARCHAR(100) NOT NULL,
    assignable_role_id   VARCHAR(100) NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    PRIMARY KEY (assigner_role_id, assignable_role_id),
    CONSTRAINT fk_role_assignment_policy_assigner
        FOREIGN KEY (assigner_role_id) REFERENCES role(role_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_role_assignment_policy_assignable
        FOREIGN KEY (assignable_role_id) REFERENCES role(role_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_role_assignment_policy_assigner
    ON role_assignment_policy (assigner_role_id);

-- -------------------------
-- USER ROLE ASSIGNMENT
-- -------------------------
CREATE TABLE IF NOT EXISTS user_role_assignment (
    assignment_id        VARCHAR(100) PRIMARY KEY,
    user_id              VARCHAR(100) NOT NULL,
    role_id              VARCHAR(100) NOT NULL,
    status               VARCHAR(30)  NOT NULL,
    assigned_by          VARCHAR(100) NOT NULL,
    assigned_at          TIMESTAMP    NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    CONSTRAINT fk_user_role_assignment_user
        FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_role_assignment_role
        FOREIGN KEY (role_id) REFERENCES role(role_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_user_role_assignment UNIQUE (user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_role_assignment_user_id ON user_role_assignment (user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_assignment_role_id ON user_role_assignment (role_id);

-- -------------------------
-- LOGIN ATTEMPTS
-- -------------------------
CREATE TABLE IF NOT EXISTS user_login_attempt (
    attempt_id           VARCHAR(100) PRIMARY KEY,
    user_id              VARCHAR(100) NOT NULL,
    ip_address           VARCHAR(100) NOT NULL,
    success              BOOLEAN      NOT NULL,
    attempt_at           TIMESTAMP    NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    CONSTRAINT fk_user_login_attempt_user
        FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON DELETE CASCADE
);

ALTER TABLE user_login_attempt
    ADD COLUMN IF NOT EXISTS attempt_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_user_login_attempt_user_id ON user_login_attempt (user_id);
CREATE INDEX IF NOT EXISTS idx_user_login_attempt_attempt_at ON user_login_attempt (attempt_at);

-- -------------------------
-- USER SESSION
-- -------------------------
CREATE TABLE IF NOT EXISTS user_session (
    session_id                 VARCHAR(100) PRIMARY KEY,
    user_id                    VARCHAR(100) NOT NULL,
    device_id                  VARCHAR(200),
    device_name                VARCHAR(200),
    device_type                VARCHAR(100),
    ip_address                 VARCHAR(100),
    access_jti                 VARCHAR(100) NOT NULL UNIQUE,
    refresh_jti                VARCHAR(100) NOT NULL UNIQUE,
    issued_at                  TIMESTAMP    NOT NULL,
    access_token_expires_at    TIMESTAMP    NOT NULL,
    refresh_token_expires_at   TIMESTAMP    NOT NULL,
    last_seen_at               TIMESTAMP    NOT NULL,
    status                     VARCHAR(30)  NOT NULL,
    revoked_at                 TIMESTAMP,
    revocation_reason          VARCHAR(255),
    created_at                 TIMESTAMP    NOT NULL,
    updated_at                 TIMESTAMP    NOT NULL,
    CONSTRAINT fk_user_session_user
        FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON DELETE CASCADE
);

ALTER TABLE user_session
    ADD COLUMN IF NOT EXISTS issued_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_seen_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS revocation_reason VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE user_session
    DROP COLUMN IF EXISTS organization_id,
    DROP COLUMN IF EXISTS roles,
    DROP COLUMN IF EXISTS expires_at;

CREATE INDEX IF NOT EXISTS idx_user_session_user_id ON user_session (user_id);
CREATE INDEX IF NOT EXISTS idx_user_session_status ON user_session (status);
CREATE INDEX IF NOT EXISTS idx_user_session_refresh_jti ON user_session (refresh_jti);
CREATE INDEX IF NOT EXISTS idx_user_session_access_jti ON user_session (access_jti);

-- -------------------------
-- SECURITY AUDIT
-- -------------------------
CREATE TABLE IF NOT EXISTS auth_audit (
    audit_id              VARCHAR(100) PRIMARY KEY,
    event_type            VARCHAR(100) NOT NULL,
    user_id               VARCHAR(100),
    session_id            VARCHAR(100),
    ip_address            VARCHAR(100),
    device_id             VARCHAR(200),
    result                VARCHAR(30)  NOT NULL,
    payload               JSONB,
    created_at            TIMESTAMP    NOT NULL,
    CONSTRAINT fk_auth_audit_user
        FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_auth_audit_session
        FOREIGN KEY (session_id) REFERENCES user_session(session_id)
        ON DELETE SET NULL
);

ALTER TABLE auth_audit
    ADD COLUMN IF NOT EXISTS result VARCHAR(30),
    ADD COLUMN IF NOT EXISTS payload JSONB,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_auth_audit_event_type ON auth_audit (event_type);
CREATE INDEX IF NOT EXISTS idx_auth_audit_user_id ON auth_audit (user_id);
CREATE INDEX IF NOT EXISTS idx_auth_audit_created_at ON auth_audit (created_at);

-- -------------------------
-- OUTBOX
-- -------------------------
CREATE TABLE IF NOT EXISTS outbox_event (
    event_id              VARCHAR(100) PRIMARY KEY,
    aggregate_type        VARCHAR(100) NOT NULL,
    aggregate_id          VARCHAR(100) NOT NULL,
    event_type            VARCHAR(150) NOT NULL,
    payload               JSONB        NOT NULL,
    status                VARCHAR(30)  NOT NULL,
    occurred_at           TIMESTAMP    NOT NULL,
    published_at          TIMESTAMP,
    retry_count           INTEGER      NOT NULL DEFAULT 0,
    last_error            TEXT,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL
);

ALTER TABLE outbox_event
    ADD COLUMN IF NOT EXISTS retry_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_error TEXT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_outbox_event_status_occurred_at ON outbox_event (status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_outbox_event_aggregate ON outbox_event (aggregate_type, aggregate_id);

-- -------------------------
-- PROCESSED EVENT (idempotency)
-- -------------------------
CREATE TABLE IF NOT EXISTS processed_event (
    processed_event_id    VARCHAR(100) PRIMARY KEY,
    event_id              VARCHAR(100) NOT NULL,
    consumer_name         VARCHAR(150) NOT NULL,
    processed_at          TIMESTAMP    NOT NULL,
    CONSTRAINT uk_processed_event UNIQUE (event_id, consumer_name)
);

-- -------------------------
-- ROLE SEED
-- -------------------------
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

COMMIT;
