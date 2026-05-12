-- Emergency break-glass procedure for recovering the exclusive SYSTEM_ADMIN role.
-- Run manually with a PostgreSQL admin connection. This script is intentionally
-- not executed by the service and is not exposed through any HTTP endpoint.
--
-- Usage:
--   psql "$DATABASE_URL" \
--     -v target_user_id='usr-target' \
--     -v actor='BREAK_GLASS_OPERATOR' \
--     -f scripts/operations/break-glass-system-admin.sql

\set ON_ERROR_STOP on

\if :{?target_user_id}
\else
\error 'target_user_id psql variable is required'
\endif

\if :{?actor}
\else
\set actor 'BREAK_GLASS'
\endif

BEGIN;

SELECT pg_advisory_xact_lock(hashtext('identity-access:break-glass:SYSTEM_ADMIN'));

SELECT role_id AS system_admin_role_id
FROM role
WHERE role_code = 'SYSTEM_ADMIN'
  AND status = 'ACTIVE'
FOR UPDATE
\gset

\if :{?system_admin_role_id}
\else
\error 'SYSTEM_ADMIN role does not exist or is not active'
\endif

SELECT user_id AS target_account_id
FROM user_account
WHERE user_id = :'target_user_id'
FOR UPDATE
\gset

\if :{?target_account_id}
\else
\error 'target user does not exist'
\endif

UPDATE user_account
SET status = 'ACTIVE',
    updated_at = NOW()
WHERE user_id = :'target_user_id';

UPDATE user_role_assignment
SET status = 'REVOKED',
    updated_at = NOW()
WHERE role_id = :'system_admin_role_id'
  AND status = 'ACTIVE'
  AND user_id <> :'target_user_id';

INSERT INTO user_role_assignment (
    assignment_id,
    user_id,
    role_id,
    status,
    assigned_by,
    assigned_at,
    created_at,
    updated_at
) VALUES (
    'break-glass-' || md5(random()::text || clock_timestamp()::text),
    :'target_user_id',
    :'system_admin_role_id',
    'ACTIVE',
    :'actor',
    NOW(),
    NOW(),
    NOW()
)
ON CONFLICT (user_id, role_id) DO UPDATE
SET status = 'ACTIVE',
    assigned_by = EXCLUDED.assigned_by,
    assigned_at = EXCLUDED.assigned_at,
    updated_at = EXCLUDED.updated_at;

INSERT INTO auth_audit (
    audit_id,
    event_type,
    user_id,
    session_id,
    ip_address,
    device_id,
    result,
    payload,
    created_at
) VALUES (
    'audit-' || md5(random()::text || clock_timestamp()::text),
    'BREAK_GLASS_SYSTEM_ADMIN_REASSIGNED',
    :'target_user_id',
    NULL,
    NULL,
    NULL,
    'SUCCESS',
    jsonb_build_object('actor', :'actor', 'targetUserId', :'target_user_id'),
    NOW()
);

COMMIT;
