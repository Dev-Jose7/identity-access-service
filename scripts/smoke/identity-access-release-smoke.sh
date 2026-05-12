#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
RUN_ID="${RUN_ID:-$(date +%s)}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin-${RUN_ID}@example.test}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin123!}"
PUBLIC_EMAIL="${PUBLIC_EMAIL:-public-${RUN_ID}@example.test}"
PUBLIC_PASSWORD="${PUBLIC_PASSWORD:-User123!}"
ADMIN_CREATED_EMAIL="${ADMIN_CREATED_EMAIL:-operator-${RUN_ID}@example.test}"
ADMIN_CREATED_PASSWORD="${ADMIN_CREATED_PASSWORD:-Operator123!}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

request() {
  local method="$1"
  local path="$2"
  local body_file="${3:-}"
  local token="${4:-}"
  local output_file="$TMP_DIR/response.json"
  local status_file="$TMP_DIR/status.txt"

  local args=(-sS -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' -o "$output_file" -w '%{http_code}')
  if [[ -n "$token" ]]; then
    args+=(-H "Authorization: Bearer $token")
  fi
  if [[ -n "$body_file" ]]; then
    args+=(--data-binary "@$body_file")
  fi

  curl "${args[@]}" > "$status_file"
  cat "$status_file"
}

json_value() {
  local file="$1"
  local key="$2"
  python3 - "$file" "$key" <<'PY'
import json, sys
with open(sys.argv[1], encoding='utf-8') as fh:
    data = json.load(fh)
value = data
for part in sys.argv[2].split('.'):
    value = value[part]
print(value)
PY
}

expect_status() {
  local expected="$1"
  local actual="$2"
  local label="$3"
  if [[ "$actual" != "$expected" ]]; then
    echo "Smoke test failed at $label: expected HTTP $expected, got $actual" >&2
    echo "Response:" >&2
    cat "$TMP_DIR/response.json" >&2 || true
    exit 1
  fi
  echo "OK $label -> HTTP $actual"
}

status="$(curl -sS -o "$TMP_DIR/response.json" -w '%{http_code}' "$BASE_URL/.well-known/jwks.json")"
expect_status 200 "$status" 'jwks'

cat > "$TMP_DIR/register-primary.json" <<JSON
{"email":"$ADMIN_EMAIL","password":"$ADMIN_PASSWORD"}
JSON
status="$(request POST /api/v1/auth/register-primary "$TMP_DIR/register-primary.json")"
expect_status 200 "$status" 'register-primary'
ADMIN_USER_ID="$(json_value "$TMP_DIR/response.json" userId)"

cat > "$TMP_DIR/login.json" <<JSON
{"email":"$ADMIN_EMAIL","password":"$ADMIN_PASSWORD"}
JSON
status="$(request POST /api/v1/auth/login "$TMP_DIR/login.json")"
expect_status 200 "$status" 'login'
ACCESS_TOKEN="$(json_value "$TMP_DIR/response.json" accessToken)"

cat > "$TMP_DIR/register-public.json" <<JSON
{"email":"$PUBLIC_EMAIL","password":"$PUBLIC_PASSWORD"}
JSON
status="$(request POST /api/v1/auth/register "$TMP_DIR/register-public.json")"
expect_status 200 "$status" 'register-public'
PUBLIC_USER_ID="$(json_value "$TMP_DIR/response.json" userId)"

cat > "$TMP_DIR/introspect.json" <<JSON
{"token":"$ACCESS_TOKEN"}
JSON
status="$(request POST /api/v1/auth/introspect "$TMP_DIR/introspect.json")"
expect_status 200 "$status" 'introspect'
ACTIVE="$(json_value "$TMP_DIR/response.json" active)"
if [[ "$ACTIVE" != "True" && "$ACTIVE" != "true" ]]; then
  echo "Smoke test failed: introspection token is not active" >&2
  cat "$TMP_DIR/response.json" >&2
  exit 1
fi

cat > "$TMP_DIR/admin-create.json" <<JSON
{"email":"$ADMIN_CREATED_EMAIL","password":"$ADMIN_CREATED_PASSWORD","roleCode":"ACCESS_MANAGER"}
JSON
status="$(request POST /api/v1/admin/iam/accounts "$TMP_DIR/admin-create.json" "$ACCESS_TOKEN")"
expect_status 200 "$status" 'admin-create-account'
CREATED_USER_ID="$(json_value "$TMP_DIR/response.json" userId)"

status="$(request GET "/api/v1/admin/iam/accounts/$CREATED_USER_ID/permissions" '' "$ACCESS_TOKEN")"
expect_status 200 "$status" 'get-created-account-permissions'

cat <<SUMMARY
Smoke test completed.
Primary user: $ADMIN_USER_ID <$ADMIN_EMAIL>
Public user: $PUBLIC_USER_ID <$PUBLIC_EMAIL>
Admin-created user: $CREATED_USER_ID <$ADMIN_CREATED_EMAIL>
SUMMARY
