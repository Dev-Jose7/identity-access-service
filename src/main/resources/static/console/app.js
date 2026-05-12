const API = {
  health: '/actuator/health',
  jwks: '/.well-known/jwks.json',
  register: '/api/v1/auth/register',
  registerPrimary: '/api/v1/auth/register-primary',
  login: '/api/v1/auth/login',
  refresh: '/api/v1/auth/refresh',
  logout: '/api/v1/auth/logout',
  introspect: '/api/v1/auth/introspect',
  accounts: '/api/v1/admin/iam/accounts',
  sessions: '/api/v1/admin/iam/sessions',
  roles: '/api/v1/admin/iam/roles',
  permissions: '/api/v1/admin/iam/permissions'
};

const CAPABILITIES = [
  { title: 'Cuentas', permissions: ['iam.account.read', 'iam.account.create', 'iam.account.block', 'iam.account.unblock'] },
  { title: 'Acceso', permissions: ['iam.access.assign-role', 'iam.access.revoke-role', 'iam.access-profile.read'] },
  { title: 'Sesiones', permissions: ['iam.session.read', 'iam.session.revoke'] },
  { title: 'Roles', permissions: ['iam.role.read', 'iam.role.create', 'iam.role.update'] },
  { title: 'Permisos', permissions: ['iam.permission.read', 'iam.permission.create', 'iam.permission.update'] },
  { title: 'Auth', permissions: ['login/public', 'refresh/public', 'introspect/public'] }
];

const state = {
  accessToken: localStorage.getItem('ia.console.accessToken') || '',
  refreshToken: localStorage.getItem('ia.console.refreshToken') || '',
  principal: null,
  permissions: new Set(),
  roles: new Set(),
  accounts: [],
  sessions: [],
  catalogRoles: [],
  catalogPermissions: [],
  rolePermissions: new Map(),
  pendingConfirmation: null
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

function can(permission) {
  if (!permission) return true;
  return state.permissions.has(permission);
}

function setConsoleFavicon(mode = 'locked') {
  const palette = {
    active: {
      background: '#052e2b',
      shield: '#0d3b35',
      stroke: '#34d399',
      dot: '#34d399',
      text: '#ecfdf5'
    },
    warning: {
      background: '#1f1305',
      shield: '#3a2508',
      stroke: '#fbbf24',
      dot: '#fbbf24',
      text: '#fff7ed'
    },
    locked: {
      background: '#07111f',
      shield: '#0f1e33',
      stroke: '#38bdf8',
      dot: '#38bdf8',
      text: '#e8f1ff'
    }
  }[mode] || {
    background: '#07111f',
    shield: '#0f1e33',
    stroke: '#38bdf8',
    dot: '#38bdf8',
    text: '#e8f1ff'
  };

  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">
    <rect width="64" height="64" rx="18" fill="${palette.background}"/>
    <path d="M32 9 49 16v14c0 12-7.2 20.7-17 25-9.8-4.3-17-13-17-25V16l17-7Z" fill="${palette.shield}" stroke="${palette.stroke}" stroke-width="3"/>
    <circle cx="48" cy="16" r="7" fill="${palette.dot}"/>
    <text x="32" y="40" text-anchor="middle" font-family="Arial, sans-serif" font-size="20" font-weight="800" fill="${palette.text}">IA</text>
  </svg>`;

  $('#consoleFavicon')?.setAttribute('href', `data:image/svg+xml,${encodeURIComponent(svg)}`);
}

function toast(message, tone = 'ok') {
  const node = $('#toast');
  node.textContent = message;
  node.className = `toast ${tone}`;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => node.classList.add('hidden'), 4200);
}

function normalizeError(error) {
  if (!error) return 'Error desconocido';
  if (typeof error === 'string') return error;
  return error.message || error.detail || error.error || JSON.stringify(error);
}

async function request(path, options = {}) {
  const headers = new Headers(options.headers || {});
  const hasBody = options.body !== undefined && options.body !== null;
  if (hasBody && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json');
  if (options.auth !== false && state.accessToken) headers.set('Authorization', `Bearer ${state.accessToken}`);

  const response = await fetch(path, {
    method: options.method || 'GET',
    headers,
    body: hasBody && !(options.body instanceof FormData) ? JSON.stringify(options.body) : options.body
  });

  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json') ? await response.json().catch(() => null) : await response.text().catch(() => '');
  if (!response.ok) {
    const message = payload && typeof payload === 'object'
      ? (payload.message || payload.detail || payload.error || JSON.stringify(payload))
      : payload || `${response.status} ${response.statusText}`;
    throw new Error(message);
  }
  return payload;
}

function persistTokens(accessToken, refreshToken) {
  state.accessToken = accessToken || '';
  state.refreshToken = refreshToken === undefined ? state.refreshToken || '' : refreshToken || '';
  if (state.accessToken) localStorage.setItem('ia.console.accessToken', state.accessToken);
  else localStorage.removeItem('ia.console.accessToken');
  if (state.refreshToken) localStorage.setItem('ia.console.refreshToken', state.refreshToken);
  else localStorage.removeItem('ia.console.refreshToken');
}

function clearSession() {
  persistTokens('', '');
  state.principal = null;
  state.permissions = new Set();
  state.roles = new Set();
  renderAll();
}

function decodeJwt(token) {
  if (!token || !token.includes('.')) return null;
  try {
    const part = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = part.padEnd(part.length + ((4 - part.length % 4) % 4), '=');
    return JSON.parse(atob(padded));
  } catch (_) {
    return null;
  }
}

async function hydratePrincipal() {
  if (!state.accessToken) {
    clearSession();
    return;
  }
  try {
    const data = await request(API.introspect, { method: 'POST', auth: false, body: { token: state.accessToken } });
    if (!data.active) {
      const fallback = decodeJwt(state.accessToken);
      state.principal = { active: false, inactiveReason: data.inactiveReason, ...fallback };
      state.permissions = new Set();
      state.roles = new Set();
      renderAll();
      return;
    }
    state.principal = data;
    state.permissions = new Set(data.permissions || []);
    state.roles = new Set(data.roles || []);
    renderAll();
    await refreshVisibleData();
  } catch (error) {
    const fallback = decodeJwt(state.accessToken);
    state.principal = fallback ? { active: false, ...fallback } : null;
    state.permissions = new Set(fallback?.permissions || []);
    state.roles = new Set(fallback?.roles || []);
    renderAll();
    toast(`No fue posible introspectar el token: ${normalizeError(error)}`, 'warn');
  }
}

async function checkService() {
  $('#baseUrlValue').textContent = window.location.origin;
  try {
    const health = await request(API.health, { auth: false });
    $('#healthValue').textContent = health.status || 'OK';
    $('#apiStatus').textContent = 'API disponible';
    $('#apiStatus').className = 'status-pill ok';
  } catch (error) {
    $('#healthValue').textContent = 'No disponible';
    $('#apiStatus').textContent = 'API sin respuesta';
    $('#apiStatus').className = 'status-pill danger';
  }
  try {
    const jwks = await request(API.jwks, { auth: false });
    $('#jwksValue').textContent = `${(jwks.keys || []).length} key(s)`;
  } catch (_) {
    $('#jwksValue').textContent = 'No disponible';
  }
}

async function refreshVisibleData() {
  const jobs = [];
  if (can('iam.account.read')) jobs.push(loadAccounts().catch(showLoadError('cuentas')));
  if (can('iam.account.read')) jobs.push(loadSessions().catch(showLoadError('sesiones')));
  if (can('iam.role.read')) jobs.push(loadRoles().catch(showLoadError('roles')));
  if (can('iam.permission.read')) jobs.push(loadPermissions().catch(showLoadError('permisos')));
  await Promise.all(jobs);
  renderAll();
}

function showLoadError(label) {
  return error => toast(`No fue posible cargar ${label}: ${normalizeError(error)}`, 'warn');
}

async function loadAccounts() {
  state.accounts = await request(API.accounts);
}

async function loadSessions() {
  state.sessions = await request(API.sessions);
}

async function loadRoles() {
  state.catalogRoles = await request(API.roles);
}

async function loadPermissions() {
  state.catalogPermissions = await request(API.permissions);
}

async function loadRolePermissions(roleId, force = false) {
  if (!force && state.rolePermissions.has(roleId)) return state.rolePermissions.get(roleId);
  const permissions = await request(`${API.roles}/${encodeURIComponent(roleId)}/permissions`);
  state.rolePermissions.set(roleId, permissions || []);
  return permissions || [];
}

async function ensureRolesCatalog() {
  if (!state.catalogRoles.length && can('iam.role.read')) {
    await loadRoles();
  }
}

async function ensurePermissionsCatalog() {
  if (!state.catalogPermissions.length && can('iam.permission.read')) {
    await loadPermissions();
  }
}

function accountById(userId) {
  return state.accounts.find(account => account.userId === userId);
}

function currentQuery(selector) {
  return ($(selector)?.value || '').trim().toLowerCase();
}

function includesQuery(query, values) {
  if (!query) return true;
  return values.some(value => String(value ?? '').toLowerCase().includes(query));
}

function renderAll() {
  renderPrincipal();
  renderPermissions();
  renderPermissionGates();
  renderCapabilityMap();
  renderAccounts();
  renderSessions();
  renderRoles();
  renderPermissionsCatalog();
  renderMetrics();
}

function renderPrincipal() {
  const authenticated = Boolean(state.accessToken && state.principal);
  const active = state.principal?.active !== false && authenticated;
  setConsoleFavicon(active ? 'active' : authenticated ? 'warning' : 'locked');
  $('#sessionMode').textContent = active ? 'Autenticado' : 'Sin autenticar';
  $('#sessionMode').className = `chip ${active ? 'ok' : 'warn'}`;
  $('#principalTitle').textContent = active
    ? (state.principal.email || state.principal.sub || 'Principal autenticado')
    : 'Inicia sesión para gestionar IAM';
  $('#principalSubtitle').textContent = active
    ? `Sesión ${state.principal.sid || state.principal.sessionId || 'sin sid visible'} · ${state.permissions.size} permiso(s) efectivo(s)`
    : 'El cliente ajusta navegación y acciones según los permisos presentes en el access token.';
  $('#logoutButton').classList.toggle('hidden', !active);

  const meta = $('#principalMeta');
  meta.innerHTML = '';
  if (active) {
    meta.append(...[badge(`sub: ${short(state.principal.sub)}`), badge(`jti: ${short(state.principal.jti)}`), badge(`roles: ${Array.from(state.roles).join(', ') || 'sin roles'}`, 'ok')]);
  }

  $('#tokenDigest').textContent = state.accessToken
    ? JSON.stringify({
        sub: state.principal?.sub,
        sid: state.principal?.sid,
        email: state.principal?.email,
        roles: Array.from(state.roles),
        permissions: Array.from(state.permissions),
        exp: state.principal?.exp
      }, null, 2)
    : 'No hay token cargado.';
}

function renderPermissions() {
  const host = $('#permissionChips');
  if (!state.permissions.size) {
    host.className = 'chip-list muted-box';
    host.textContent = 'Sin permisos cargados.';
    return;
  }
  host.className = 'chip-list';
  host.innerHTML = '';
  Array.from(state.permissions).sort().forEach(permission => host.append(badge(permission, 'ok')));
}

function renderPermissionGates() {
  $$('[data-permission]').forEach(node => {
    const allowed = can(node.dataset.permission);
    node.classList.toggle('locked', !allowed);
    node.toggleAttribute('disabled', !allowed && node.matches('button'));
    node.title = allowed ? '' : `Requiere ${node.dataset.permission}`;
  });
}

function renderCapabilityMap() {
  const host = $('#capabilityMap');
  host.innerHTML = '';
  CAPABILITIES.forEach(group => {
    const card = document.createElement('article');
    card.className = 'capability-card';
    const list = group.permissions.map(permission => {
      const publicCapability = permission.endsWith('/public');
      const ok = publicCapability || can(permission);
      return `<li><span>${escapeHtml(permission)}</span><strong class="${ok ? 'yes' : 'no'}">${ok ? 'OK' : 'NO'}</strong></li>`;
    }).join('');
    card.innerHTML = `<strong>${escapeHtml(group.title)}</strong><ul>${list}</ul>`;
    host.append(card);
  });
}

function renderMetrics() {
  $('#accountsCount').textContent = can('iam.account.read') ? String(state.accounts.length) : '-';
  $('#sessionsCount').textContent = can('iam.account.read') ? String(state.sessions.length) : '-';
  const roles = can('iam.role.read') ? state.catalogRoles.length : 0;
  const permissions = can('iam.permission.read') ? state.catalogPermissions.length : 0;
  $('#catalogCount').textContent = roles || permissions ? `${roles}/${permissions}` : '-';
}

function renderAccounts() {
  const tbody = $('#accountsTable');
  if (!can('iam.account.read')) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state">Requiere iam.account.read para listar cuentas.</div></td></tr>`;
    return;
  }
  if (!state.accounts.length) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state">No hay cuentas cargadas.</div></td></tr>`;
    return;
  }
  const query = currentQuery('#accountSearch');
  const items = state.accounts.filter(account => includesQuery(query, [
    account.email,
    account.userId,
    account.status,
    account.failedLoginCount,
    ...(account.roles || [])
  ]));
  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state">No hay cuentas que coincidan con la búsqueda.</div></td></tr>`;
    return;
  }
  tbody.innerHTML = items.map(account => {
    const roles = Array.from(account.roles || []).map(role => `<span class="badge ok">${escapeHtml(role)}</span>`).join('') || '<span class="badge muted">Sin roles</span>';
    const status = String(account.status || '').toUpperCase();
    const statusAction = status === 'BLOCKED'
      ? actionButton('Desbloquear', 'unblock-account', account, 'iam.account.unblock')
      : actionButton('Bloquear', 'block-account', account, 'iam.account.block', 'danger');
    return `<tr>
      <td><strong>${escapeHtml(account.email)}</strong><br><small>${escapeHtml(account.userId)}</small></td>
      <td>${statusBadge(account.status)}</td>
      <td><div class="badge-stack">${roles}</div></td>
      <td>${escapeHtml(account.failedLoginCount ?? 0)}</td>
      <td><small>${formatDate(account.updatedAt)}</small></td>
      <td><div class="row-actions">
        ${actionButton('Permisos', 'account-permissions', account, 'iam.permission.read')}
        ${actionButton('Asignar rol', 'assign-role', account, 'iam.access.assign-role')}
        ${statusAction}
        ${actionButton('Revocar sesiones', 'revoke-sessions', account, 'iam.session.revoke', 'danger')}
      </div></td>
    </tr>`;
  }).join('');
}

function renderSessions() {
  const tbody = $('#sessionsTable');
  if (!can('iam.account.read')) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state">Requiere iam.account.read para listar sesiones.</div></td></tr>`;
    return;
  }
  if (!state.sessions.length) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state">No hay sesiones cargadas.</div></td></tr>`;
    return;
  }
  const query = currentQuery('#sessionSearch');
  const items = state.sessions.filter(session => {
    const account = accountById(session.userId);
    return includesQuery(query, [
      session.sessionId,
      session.userId,
      account?.email,
      session.status,
      session.ipAddress,
      session.deviceId,
      session.deviceName,
      session.deviceType,
      session.revocationReason,
      session.issuedAt,
      session.accessTokenExpiresAt,
      session.refreshTokenExpiresAt
    ]);
  });
  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state">No hay sesiones que coincidan con la búsqueda.</div></td></tr>`;
    return;
  }
  tbody.innerHTML = items.map(session => {
    const account = accountById(session.userId);
    const accountLabel = account?.email || short(session.userId, 22);
    return `<tr>
    <td><strong>${short(session.sessionId, 18)}</strong><br><small>${formatDate(session.issuedAt)}</small></td>
    <td class="account-cell">
      <button class="link-button" type="button" data-action="account-details" data-user-id="${escapeHtml(session.userId)}" data-email="${escapeHtml(account?.email || session.userId)}" data-permission="iam.account.read">${escapeHtml(accountLabel)}</button>
      <br><small>${escapeHtml(session.userId)}</small>
    </td>
    <td>${statusBadge(session.status)}</td>
    <td>${escapeHtml(session.ipAddress || '-')}<br><small>${escapeHtml([session.deviceName, session.deviceType].filter(Boolean).join(' / ') || session.deviceId || '-')}</small></td>
    <td><small>Access: ${formatDate(session.accessTokenExpiresAt)}<br>Refresh: ${formatDate(session.refreshTokenExpiresAt)}</small></td>
    <td>${session.revokedAt ? `<small>${formatDate(session.revokedAt)}<br>${escapeHtml(session.revocationReason || '')}</small>` : actionButton('Revocar usuario', 'revoke-session-user', { userId: session.userId, email: session.userId }, 'iam.session.revoke', 'danger')}</td>
  </tr>`;
  }).join('');
}

function renderRoles() {
  const host = $('#rolesGrid');
  if (!can('iam.role.read')) {
    host.innerHTML = `<div class="empty-state">Requiere iam.role.read para consultar el catálogo de roles.</div>`;
    return;
  }
  if (!state.catalogRoles.length) {
    host.innerHTML = `<div class="empty-state">No hay roles cargados.</div>`;
    return;
  }
  const query = currentQuery('#roleSearch');
  const items = state.catalogRoles.filter(role => includesQuery(query, [
    role.roleId,
    role.roleCode,
    role.description,
    role.status,
    role.protectedRole ? 'protegido' : 'editable'
  ]));
  if (!items.length) {
    host.innerHTML = `<div class="empty-state">No hay roles que coincidan con la búsqueda.</div>`;
    return;
  }
  host.innerHTML = items.map(role => `<article class="role-card">
    <div class="card-head"><span class="tag">Rol</span>${statusBadge(role.status)}</div>
    <h3>${escapeHtml(role.roleCode)}</h3>
    <div class="card-meta">
      <span>${escapeHtml(role.description || '-')}</span>
      <span>ID: ${escapeHtml(role.roleId)}</span>
      <span>${role.protectedRole ? 'Protegido' : 'Editable'}</span>
    </div>
    <div class="card-actions">
      ${roleButton('Ver permisos', 'role-permissions', role, 'iam.permission.read')}
      ${roleButton('Conceder permiso', 'grant-permission', role, 'iam.role.update')}
      ${roleButton('Editar', 'edit-role', role, 'iam.role.update')}
      ${role.status === 'ACTIVE' ? roleButton('Deshabilitar', 'disable-role', role, 'iam.role.update', 'danger') : ''}
    </div>
  </article>`).join('');
}

function renderPermissionsCatalog() {
  const host = $('#permissionsGrid');
  if (!can('iam.permission.read')) {
    host.innerHTML = `<div class="empty-state">Requiere iam.permission.read para consultar permisos.</div>`;
    return;
  }
  const query = ($('#permissionSearch')?.value || '').trim().toLowerCase();
  const items = state.catalogPermissions.filter(permission => !query || [permission.permissionCode, permission.resource, permission.action, permission.scope].some(value => String(value || '').toLowerCase().includes(query)));
  if (!items.length) {
    host.innerHTML = `<div class="empty-state">No hay permisos para mostrar.</div>`;
    return;
  }
  host.innerHTML = items.map(permission => `<article class="permission-card">
    <div class="card-head"><span class="tag">Permiso</span>${statusBadge(permission.status)}</div>
    <h3>${escapeHtml(permission.permissionCode)}</h3>
    <div class="card-meta">
      <span>${escapeHtml(permission.description || '-')}</span>
      <span>${escapeHtml(permission.resource)} · ${escapeHtml(permission.action)} · ${escapeHtml(permission.scope)}</span>
      <span>${permission.systemPermission ? 'Sistema' : 'Custom'} · ${escapeHtml(permission.permissionId)}</span>
    </div>
    <div class="card-actions">
      ${permissionButton('Editar', 'edit-permission', permission, 'iam.permission.update')}
      ${permission.status === 'ACTIVE' ? permissionButton('Deshabilitar', 'disable-permission', permission, 'iam.permission.update', 'danger') : ''}
    </div>
  </article>`).join('');
}

function actionButton(label, action, account, permission, tone = '') {
  const disabled = can(permission) ? '' : 'disabled';
  return `<button class="btn compact ${tone}" type="button" data-action="${action}" data-user-id="${escapeHtml(account.userId)}" data-email="${escapeHtml(account.email || account.userId)}" data-permission="${permission}" ${disabled}>${label}</button>`;
}

function roleButton(label, action, role, permission, tone = '') {
  const disabled = can(permission) ? '' : 'disabled';
  return `<button class="btn compact ${tone}" type="button" data-action="${action}" data-role-id="${escapeHtml(role.roleId)}" data-role-code="${escapeHtml(role.roleCode)}" data-description="${escapeHtml(role.description || '')}" data-permission="${permission}" ${disabled}>${label}</button>`;
}

function permissionButton(label, action, permissionItem, permission, tone = '') {
  const disabled = can(permission) ? '' : 'disabled';
  return `<button class="btn compact ${tone}" type="button" data-action="${action}" data-permission-id="${escapeHtml(permissionItem.permissionId)}" data-permission-code="${escapeHtml(permissionItem.permissionCode)}" data-resource="${escapeHtml(permissionItem.resource)}" data-action-value="${escapeHtml(permissionItem.action)}" data-scope="${escapeHtml(permissionItem.scope)}" data-description="${escapeHtml(permissionItem.description || '')}" data-permission="${permission}" ${disabled}>${label}</button>`;
}

function statusBadge(status) {
  const normalized = String(status || 'UNKNOWN').toUpperCase();
  const tone = normalized === 'ACTIVE' ? 'ok' : normalized === 'REVOKED' || normalized === 'BLOCKED' || normalized === 'DISABLED' ? 'danger' : 'warn';
  return `<span class="badge ${tone}">${escapeHtml(normalized)}</span>`;
}

function badge(text, tone = 'muted') {
  const node = document.createElement('span');
  node.className = `badge ${tone}`;
  node.textContent = text || '-';
  return node;
}

function short(value, length = 14) {
  const text = String(value || '-');
  return text.length > length ? `${text.slice(0, length)}...` : text;
}

function formatDate(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString();
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char]));
}

function populateSelect(select, options, selectedValue, placeholder = 'Selecciona una opción') {
  select.innerHTML = '';
  const placeholderOption = document.createElement('option');
  placeholderOption.value = '';
  placeholderOption.textContent = placeholder;
  select.append(placeholderOption);
  options.forEach(option => {
    const node = document.createElement('option');
    node.value = option.value;
    node.textContent = option.label;
    select.append(node);
  });
  select.value = selectedValue || '';
}

function populateCatalogControls(form, seed = {}) {
  const roleOptions = state.catalogRoles
    .filter(role => String(role.status || '').toUpperCase() === 'ACTIVE')
    .map(role => ({
      value: role.roleCode,
      label: `${role.roleCode}${role.protectedRole ? ' · protegido' : ''}${role.description ? ` · ${role.description}` : ''}`
    }));
  $$('[data-role-select]', form).forEach(select => populateSelect(
    select,
    roleOptions,
    seed.roleCode,
    select.dataset.optionalLabel || 'Selecciona un rol'));

  const roleIdOptions = state.catalogRoles
    .filter(role => String(role.status || '').toUpperCase() === 'ACTIVE')
    .map(role => ({
      value: role.roleId,
      label: `${role.roleCode}${role.protectedRole ? ' · protegido' : ''}${role.description ? ` · ${role.description}` : ''}`
    }));
  $$('[data-role-id-select]', form).forEach(select => populateSelect(
    select,
    roleIdOptions,
    seed.roleId,
    'Selecciona un rol'));

  const permissionOptions = state.catalogPermissions
    .filter(permission => String(permission.status || '').toUpperCase() === 'ACTIVE')
    .map(permission => ({
      value: permission.permissionCode,
      label: `${permission.permissionCode} · ${permission.resource}.${permission.action} · ${permission.scope}`
    }));
  $$('[data-permission-select]', form).forEach(select => populateSelect(
    select,
    permissionOptions,
    seed.permissionCode,
    'Selecciona un permiso'));
}

async function openModalWithCatalog(id, seed = {}) {
  if (id === 'adminCreateModal' || id === 'assignRoleModal') {
    await ensureRolesCatalog().catch(showLoadError('roles'));
  }
  if (id === 'grantPermissionModal') {
    await Promise.all([
      ensureRolesCatalog().catch(showLoadError('roles')),
      ensurePermissionsCatalog().catch(showLoadError('permisos'))
    ]);
  }
  openModal(id, seed);
}

function openModal(id, seed = {}) {
  const modal = $(`#${id}`);
  if (!modal) return;
  const form = $('form', modal);
  if (form) populateCatalogControls(form, seed);
  Object.entries(seed).forEach(([name, value]) => {
    const input = form?.elements?.[name];
    if (input) input.value = value || '';
  });
  modal.showModal();
}

function closeModal(form) {
  form.closest('dialog')?.close();
}

function openConfirmation({ title, message, confirmLabel = 'Confirmar', tone = 'danger', execute }) {
  state.pendingConfirmation = execute;
  $('#confirmActionTitle').textContent = title;
  $('#confirmActionMessage').textContent = message;
  const button = $('#confirmActionButton');
  button.textContent = confirmLabel;
  button.className = `btn ${tone}`;
  openModal('confirmActionModal');
}

function formData(form) {
  const data = Object.fromEntries(new FormData(form).entries());
  $$('input[type="checkbox"]', form).forEach(input => data[input.name] = input.checked);
  Object.keys(data).forEach(key => {
    if (typeof data[key] === 'string') data[key] = data[key].trim();
    if (data[key] === '') delete data[key];
  });
  return data;
}

async function handleForm(form) {
  const type = form.dataset.form;
  const data = formData(form);
  const submit = $('button[type="submit"]:not([value])', form) || $('button.primary, button.danger', form);
  submit?.setAttribute('disabled', 'disabled');
  try {
    if (type === 'login') {
      const result = await request(API.login, { method: 'POST', auth: false, body: data });
      persistTokens(result.accessToken, result.refreshToken);
      closeModal(form);
      toast('Sesión iniciada.');
      await hydratePrincipal();
    }
    if (type === 'register-primary') {
      const result = await request(API.registerPrimary, { method: 'POST', auth: false, body: data });
      closeModal(form);
      toast(`Cuenta primaria creada: ${result.email}`);
    }
    if (type === 'register-public') {
      const result = await request(API.register, { method: 'POST', auth: false, body: data });
      closeModal(form);
      toast(`Cuenta pública creada: ${result.email}`);
    }
    if (type === 'admin-create') {
      const result = await request(API.accounts, { method: 'POST', body: data });
      closeModal(form);
      toast(`Cuenta creada: ${result.email}`);
      await loadAccounts();
      renderAll();
    }
    if (type === 'assign-role') {
      const result = await request(`${API.accounts}/${encodeURIComponent(data.userId)}/roles`, { method: 'POST', body: { roleCode: data.roleCode } });
      closeModal(form);
      toast(`Rol ${result.roleCode} procesado para la cuenta.`);
      await loadAccounts();
      renderAll();
    }
    if (type === 'block-account') {
      const result = await request(`${API.accounts}/${encodeURIComponent(data.userId)}/block`, { method: 'POST', body: { reason: data.reason } });
      closeModal(form);
      toast(`Cuenta ${result.userId} quedó en estado ${result.status}.`);
      await loadAccounts();
      if (can('iam.account.read')) await loadSessions().catch(showLoadError('sesiones'));
      renderAll();
    }
    if (type === 'unblock-account') {
      const result = await request(`${API.accounts}/${encodeURIComponent(data.userId)}/unblock`, { method: 'POST', body: { reason: data.reason } });
      closeModal(form);
      toast(`Cuenta ${result.userId} quedó en estado ${result.status}.`);
      await loadAccounts();
      renderAll();
    }
    if (type === 'revoke-sessions') {
      const result = await request(`${API.accounts}/${encodeURIComponent(data.userId)}/sessions/revoke`, { method: 'POST', body: { reason: data.reason } });
      closeModal(form);
      toast(`${result.revokedSessions} sesión(es) revocada(s).`);
      await loadSessions();
      renderAll();
    }
    if (type === 'create-role') {
      await request(API.roles, { method: 'POST', body: data });
      closeModal(form);
      toast('Rol creado.');
      await loadRoles();
      renderAll();
    }
    if (type === 'edit-role') {
      await request(`${API.roles}/${encodeURIComponent(data.roleId)}`, {
        method: 'PATCH',
        body: { description: data.description }
      });
      closeModal(form);
      toast('Rol actualizado.');
      await loadRoles();
      renderAll();
    }
    if (type === 'create-permission') {
      await request(API.permissions, { method: 'POST', body: data });
      closeModal(form);
      toast('Permiso creado.');
      await loadPermissions();
      renderAll();
    }
    if (type === 'edit-permission') {
      await request(`${API.permissions}/${encodeURIComponent(data.permissionId)}`, {
        method: 'PATCH',
        body: {
          resource: data.resource,
          action: data.action,
          scope: data.scope || 'GLOBAL',
          description: data.description
        }
      });
      closeModal(form);
      toast('Permiso actualizado.');
      await loadPermissions();
      renderAll();
    }
    if (type === 'grant-permission') {
      await request(`${API.roles}/${encodeURIComponent(data.roleId)}/permissions`, { method: 'POST', body: { permissionCode: data.permissionCode } });
      state.rolePermissions.delete(data.roleId);
      closeModal(form);
      toast('Permiso concedido al rol.');
    }
    if (type === 'confirm-action') {
      if (typeof state.pendingConfirmation !== 'function') {
        closeModal(form);
        return;
      }
      await state.pendingConfirmation();
      state.pendingConfirmation = null;
      closeModal(form);
    }
  } catch (error) {
    toast(normalizeError(error), 'danger');
  } finally {
    submit?.removeAttribute('disabled');
  }
}

async function inspectCurrentToken() {
  const token = $('#introspectToken').value.trim();
  if (!token) {
    toast('Pega un token para introspectar.', 'warn');
    return;
  }
  try {
    const result = await request(API.introspect, { method: 'POST', auth: false, body: { token } });
    $('#introspectOutput').textContent = JSON.stringify(result, null, 2);
  } catch (error) {
    $('#introspectOutput').textContent = normalizeError(error);
    toast('La introspección falló.', 'danger');
  }
}

async function refreshToken() {
  if (!state.refreshToken) {
    toast('No hay refresh token cargado.', 'warn');
    return;
  }
  try {
    const result = await request(API.refresh, { method: 'POST', auth: false, body: { refreshToken: state.refreshToken } });
    persistTokens(result.accessToken, result.refreshToken);
    toast('Token renovado.');
    await hydratePrincipal();
  } catch (error) {
    toast(normalizeError(error), 'danger');
  }
}

async function logout() {
  try {
    await request(API.logout, { method: 'POST' });
    toast('Sesión cerrada.');
  } catch (error) {
    toast(`Logout remoto falló: ${normalizeError(error)}`, 'warn');
  } finally {
    clearSession();
  }
}

async function copyToken() {
  if (!state.accessToken) return toast('No hay access token para copiar.', 'warn');
  await navigator.clipboard.writeText(state.accessToken);
  toast('Access token copiado.');
}

async function handleTableAction(button) {
  const action = button.dataset.action;
  if (button.dataset.permission && !can(button.dataset.permission)) {
    toast(`Requiere ${button.dataset.permission}`, 'warn');
    return;
  }
  if (action === 'block-account') openModal('blockAccountModal', { userId: button.dataset.userId, email: button.dataset.email });
  if (action === 'unblock-account') openModal('unblockAccountModal', { userId: button.dataset.userId, email: button.dataset.email });
  if (action === 'revoke-sessions' || action === 'revoke-session-user') openModal('revokeSessionsModal', { userId: button.dataset.userId, email: button.dataset.email });
  if (action === 'account-details') await showAccountDetails(button.dataset.userId);
  if (action === 'account-permissions') await showAccountPermissions(button.dataset.userId);
  if (action === 'role-permissions') await showRolePermissions(button.dataset.roleId, button.dataset.roleCode);
  if (action === 'assign-role') await openModalWithCatalog('assignRoleModal', { userId: button.dataset.userId, email: button.dataset.email });
  if (action === 'grant-permission') await openModalWithCatalog('grantPermissionModal', { roleId: button.dataset.roleId, roleCode: button.dataset.roleCode });
  if (action === 'edit-role') await editRole(button.dataset.roleId, button.dataset.roleCode, button.dataset.description);
  if (action === 'disable-role') await disableRole(button.dataset.roleId, button.dataset.roleCode);
  if (action === 'edit-permission') await editPermission(button.dataset);
  if (action === 'disable-permission') await disablePermission(button.dataset.permissionId, button.dataset.permissionCode);
}

async function showAccountDetails(userId) {
  try {
    if (!state.accounts.length) await loadAccounts();
    const account = accountById(userId);
    const body = $('#accountDetailsBody');
    if (!account) {
      body.innerHTML = `<div class="empty-state">No se encontró la cuenta ${escapeHtml(userId)} en el listado actual.</div>`;
      openModal('accountDetailsModal');
      return;
    }
    const roles = Array.from(account.roles || []).map(role => `<span class="badge ok">${escapeHtml(role)}</span>`).join('') || '<span class="badge muted">Sin roles</span>';
    body.innerHTML = `
      ${detailRow('Email', account.email)}
      ${detailRow('User ID', account.userId)}
      ${detailRow('Estado', statusBadge(account.status), true)}
      ${detailRow('Roles', `<div class="badge-stack">${roles}</div>`, true)}
      ${detailRow('Fallos login', account.failedLoginCount ?? 0)}
      ${detailRow('Creada', formatDate(account.createdAt))}
      ${detailRow('Actualizada', formatDate(account.updatedAt))}
    `;
    openModal('accountDetailsModal');
  } catch (error) {
    toast(normalizeError(error), 'danger');
  }
}

function detailRow(label, value, html = false) {
  const content = html ? (value ?? '-') : escapeHtml(value ?? '-');
  return `<div class="detail-row"><span>${escapeHtml(label)}</span><strong>${content}</strong></div>`;
}

async function showAccountPermissions(userId) {
  try {
    const result = await request(`${API.accounts}/${encodeURIComponent(userId)}/permissions`);
    const account = accountById(userId);
    showAccessDetails({
      title: 'Permisos efectivos de cuenta',
      subtitle: account?.email || result.userId || userId,
      sections: [
        { title: 'Roles activos', items: Array.from(result.roles || []), tone: 'ok' },
        { title: 'Permisos efectivos', items: Array.from(result.permissions || []), tone: 'muted' }
      ]
    });
  } catch (error) {
    toast(normalizeError(error), 'danger');
  }
}

async function showRolePermissions(roleId, roleCode) {
  try {
    const permissions = await loadRolePermissions(roleId, true);
    showAccessDetails({
      title: 'Permisos concedidos al rol',
      subtitle: `${roleCode || 'Rol'} · ${roleId}`,
      sections: [
        {
          title: 'Permisos',
          items: permissions,
          tone: 'muted',
          formatter: permission => `
            <article class="permission-row">
              <strong>${escapeHtml(permission.permissionCode)}</strong>
              <span>${escapeHtml(permission.resource)} · ${escapeHtml(permission.action)} · ${escapeHtml(permission.scope)}</span>
              <small>${escapeHtml(permission.description || '-')}</small>
            </article>`
        }
      ]
    });
  } catch (error) {
    toast(normalizeError(error), 'danger');
  }
}

function showAccessDetails({ title, subtitle, sections }) {
  $('#accessDetailsTitle').textContent = title;
  $('#accessDetailsSubtitle').textContent = subtitle || '';
  $('#accessDetailsBody').innerHTML = sections.map(section => {
    const items = Array.from(section.items || []);
    const content = items.length
      ? `<div class="${section.formatter ? 'permission-list' : 'badge-stack'}">${items.map(item => section.formatter
          ? section.formatter(item)
          : `<span class="badge ${section.tone || 'muted'}">${escapeHtml(item)}</span>`).join('')}</div>`
      : '<div class="empty-state compact-empty">Sin datos para mostrar.</div>';
    return `<section class="info-section">
      <h4>${escapeHtml(section.title)}</h4>
      ${content}
    </section>`;
  }).join('');
  openModal('accessDetailsModal');
}

async function editRole(roleId, roleCode, currentDescription) {
  const role = state.catalogRoles.find(item => item.roleId === roleId);
  openModal('editRoleModal', {
    roleId,
    roleCode: roleCode || role?.roleCode || '',
    description: currentDescription || role?.description || ''
  });
}

async function disableRole(roleId, roleCode) {
  openConfirmation({
    title: 'Deshabilitar rol',
    message: `Vas a deshabilitar el rol ${roleCode}. Esta acción impide nuevas asignaciones y puede afectar el acceso efectivo.`,
    confirmLabel: 'Deshabilitar rol',
    execute: async () => {
      await request(`${API.roles}/${encodeURIComponent(roleId)}/disable`, { method: 'POST' });
      toast('Rol deshabilitado.');
      await loadRoles();
      renderAll();
    }
  });
}

async function editPermission(dataset) {
  openModal('editPermissionModal', {
    permissionId: dataset.permissionId,
    permissionCode: dataset.permissionCode,
    resource: dataset.resource,
    action: dataset.actionValue,
    scope: dataset.scope || 'GLOBAL',
    description: dataset.description
  });
}

async function disablePermission(permissionId, permissionCode) {
  openConfirmation({
    title: 'Deshabilitar permiso',
    message: `Vas a deshabilitar el permiso ${permissionCode}. Los roles que dependan de este permiso pueden perder esa capacidad.`,
    confirmLabel: 'Deshabilitar permiso',
    execute: async () => {
      await request(`${API.permissions}/${encodeURIComponent(permissionId)}/disable`, { method: 'POST' });
      toast('Permiso deshabilitado.');
      await loadPermissions();
      renderAll();
    }
  });
}

function switchSection(section) {
  $$('.section-panel').forEach(panel => panel.classList.remove('active'));
  $(`#section-${section}`)?.classList.add('active');
  $$('.nav-item').forEach(item => item.classList.toggle('active', item.dataset.section === section));
}

function attachEvents() {
  $$('[data-open-modal]').forEach(button => button.addEventListener('click', () => {
    openModalWithCatalog(button.dataset.openModal).catch(error => toast(normalizeError(error), 'danger'));
  }));
  $$('.modal form').forEach(form => form.addEventListener('submit', event => {
    event.preventDefault();
    handleForm(form);
  }));
  $$('[data-close-modal]').forEach(button => button.addEventListener('click', () => {
    const dialog = button.closest('dialog');
    if (dialog?.id === 'confirmActionModal') state.pendingConfirmation = null;
    dialog?.close();
  }));
  $$('.nav-item').forEach(item => item.addEventListener('click', () => {
    if (item.dataset.permission && !can(item.dataset.permission)) return toast(`Requiere ${item.dataset.permission}`, 'warn');
    switchSection(item.dataset.section);
  }));
  $$('[data-refresh]').forEach(button => button.addEventListener('click', async () => {
    const target = button.dataset.refresh;
    try {
      if (target === 'accounts') await loadAccounts();
      if (target === 'sessions') await loadSessions();
      if (target === 'roles') await loadRoles();
      if (target === 'permissions') await loadPermissions();
      renderAll();
      toast('Datos actualizados.');
    } catch (error) {
      toast(normalizeError(error), 'danger');
    }
  }));
  document.addEventListener('click', event => {
    const action = event.target.closest('[data-action]');
    if (action) handleTableAction(action);
  });
  $('#refreshAllButton').addEventListener('click', async () => { await checkService(); await refreshVisibleData(); toast('Consola sincronizada.'); });
  $('#refreshTokenButton').addEventListener('click', refreshToken);
  $('#copyTokenButton').addEventListener('click', copyToken);
  $('#logoutButton').addEventListener('click', logout);
  $('#introspectButton').addEventListener('click', inspectCurrentToken);
  $('#useCurrentTokenButton').addEventListener('click', () => { $('#introspectToken').value = state.accessToken || ''; });
  $('#accountSearch').addEventListener('input', renderAccounts);
  $('#sessionSearch').addEventListener('input', renderSessions);
  $('#roleSearch').addEventListener('input', renderRoles);
  $('#permissionSearch').addEventListener('input', renderPermissionsCatalog);
}

async function boot() {
  attachEvents();
  renderAll();
  await checkService();
  if (state.accessToken) await hydratePrincipal();
}

boot();
