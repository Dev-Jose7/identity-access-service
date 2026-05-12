# Break-Glass: SYSTEM_ADMIN Recovery

This service protects the highest-privilege role with an exclusive assignment rule: only one active account can hold `SYSTEM_ADMIN` at a time.

Use this procedure only when the current `SYSTEM_ADMIN` account is inaccessible, blocked, lost, or compromised and the system needs administrative recovery.

## Guarantees

- The procedure is not exposed as an HTTP endpoint.
- It requires direct PostgreSQL administrative access.
- It revokes any currently active `SYSTEM_ADMIN` assignment before activating the target assignment.
- It writes an `auth_audit` row with event type `BREAK_GLASS_SYSTEM_ADMIN_REASSIGNED`.

## Preconditions

- The target account already exists in `user_account`.
- The operator has direct database access approved by your operational process.
- The action is tracked in your incident/change-management process.

## Command

```bash
psql "$DATABASE_URL" \
  -v target_user_id='usr-target' \
  -v actor='BREAK_GLASS_OPERATOR' \
  -f scripts/operations/break-glass-system-admin.sql
```

## After Running

1. Log in with the recovered account.
2. Rotate the recovered account password if needed.
3. Review `auth_audit` for `BREAK_GLASS_SYSTEM_ADMIN_REASSIGNED`.
4. Review `user_role_assignment` and confirm exactly one active `SYSTEM_ADMIN` assignment remains.
5. Revoke active sessions for compromised accounts if the recovery was triggered by compromise.
