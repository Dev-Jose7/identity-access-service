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

CREATE INDEX IF NOT EXISTS idx_user_account_email ON user_account (email);
CREATE INDEX IF NOT EXISTS idx_user_account_status ON user_account (status);

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

CREATE TABLE IF NOT EXISTS role (
    role_id              VARCHAR(100) PRIMARY KEY,
    role_code            VARCHAR(100) NOT NULL UNIQUE,
    description          VARCHAR(255) NOT NULL,
    status               VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    protected_role       BOOLEAN      NOT NULL DEFAULT FALSE,
    exclusive_assignment BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL
);

ALTER TABLE role
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS protected_role BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS exclusive_assignment BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_role_status ON role (status);

CREATE TABLE IF NOT EXISTS permission (
    permission_id        VARCHAR(100) PRIMARY KEY,
    permission_code      VARCHAR(150) NOT NULL UNIQUE,
    resource             VARCHAR(100) NOT NULL,
    action               VARCHAR(100) NOT NULL,
    scope                VARCHAR(50)  NOT NULL DEFAULT 'GLOBAL',
    description          VARCHAR(255) NOT NULL,
    status               VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    system_permission    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_permission_status ON permission (status);
CREATE INDEX IF NOT EXISTS idx_permission_resource_action ON permission (resource, action);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id              VARCHAR(100) NOT NULL,
    permission_code      VARCHAR(150) NOT NULL,
    resource             VARCHAR(100) NOT NULL,
    action               VARCHAR(100) NOT NULL,
    scope                VARCHAR(50)  NOT NULL DEFAULT 'GLOBAL',
    created_at           TIMESTAMP    NOT NULL,
    PRIMARY KEY (role_id, permission_code, resource, action, scope),
    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id) REFERENCES role(role_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_code) REFERENCES permission(permission_code)
        ON DELETE CASCADE
);

ALTER TABLE role_permission
    ALTER COLUMN scope SET DEFAULT 'GLOBAL';

INSERT INTO permission (
    permission_id,
    permission_code,
    resource,
    action,
    scope,
    description,
    status,
    system_permission,
    created_at,
    updated_at
)
SELECT
    'permission-' || md5(rp.permission_code),
    rp.permission_code,
    rp.resource,
    rp.action,
    rp.scope,
    rp.permission_code,
    'ACTIVE',
    TRUE,
    NOW(),
    NOW()
FROM role_permission rp
WHERE NOT EXISTS (
    SELECT 1
    FROM permission p
    WHERE p.permission_code = rp.permission_code
)
ON CONFLICT (permission_code) DO NOTHING;

ALTER TABLE role_permission
    DROP CONSTRAINT IF EXISTS fk_role_permission_permission;

ALTER TABLE role_permission
    ADD CONSTRAINT fk_role_permission_permission
    FOREIGN KEY (permission_code) REFERENCES permission(permission_code)
    ON DELETE CASCADE;

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

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_role_assignment_single_active_system_admin
    ON user_role_assignment (role_id)
    WHERE status = 'ACTIVE'
      AND role_id = '9de6fc0e-13d2-42a1-9232-4df6cdbf6f31';

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

CREATE INDEX IF NOT EXISTS idx_user_session_user_id ON user_session (user_id);
CREATE INDEX IF NOT EXISTS idx_user_session_status ON user_session (status);
CREATE INDEX IF NOT EXISTS idx_user_session_refresh_jti ON user_session (refresh_jti);
CREATE INDEX IF NOT EXISTS idx_user_session_access_jti ON user_session (access_jti);

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

CREATE TABLE IF NOT EXISTS processed_event (
    processed_event_id    VARCHAR(100) PRIMARY KEY,
    event_id              VARCHAR(100) NOT NULL,
    consumer_name         VARCHAR(150) NOT NULL,
    processed_at          TIMESTAMP    NOT NULL,
    CONSTRAINT uk_processed_event UNIQUE (event_id, consumer_name)
);
