const TOKEN_KEY = 'mc_token';
const USER_KEY = 'mc_user';

function tokenExpiry(token) {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(payload)).exp * 1000;
  } catch {
    return 0;
  }
}

export function saveSession({ token, user }) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export const saveUser = (user) => localStorage.setItem(USER_KEY, JSON.stringify(user));
export const getToken = () => localStorage.getItem(TOKEN_KEY);

export function getUser() {
  try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch { return null; }
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function isLoggedIn() {
  const token = getToken();
  if (!token) return false;
  if (tokenExpiry(token) <= Date.now()) { clearSession(); return false; }
  return true;
}

/** UI hint only. The server enforces real authorization on every /api/admin call. */
export const isAdmin = () => isLoggedIn() && !!getUser()?.roles?.includes('ROLE_ADMIN');

export function logout(to = '/') {
  clearSession();
  location.href = to;
}

/** Only allow same-site relative paths, which prevents open-redirect abuse. */
export function safeRedirect(target, fallback = '/') {
  return typeof target === 'string' && target.startsWith('/') && !target.startsWith('//') && !target.startsWith('/\\')
    ? target : fallback;
}

export function redirectToLogin(reason) {
  const base = location.pathname.startsWith('/admin/') ? '/admin/login.html' : '/pages/login.html';
  if (location.pathname === base) return;
  const params = new URLSearchParams();
  if (reason) params.set('reason', reason);
  params.set('redirect', location.pathname + location.search);
  location.href = `${base}?${params}`;
}

/** Page guards: `if (!requireLogin()) return;` inside the page's init function. */
export function requireLogin() {
  if (isLoggedIn()) return true;
  redirectToLogin();
  return false;
}

export function requireAdmin() {
  if (isAdmin()) return true;
  redirectToLogin();
  return false;
}