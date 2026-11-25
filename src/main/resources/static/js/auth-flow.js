import { saveSession, isLoggedIn } from '/js/auth.js';
import { getParam } from '/js/ui.js';

/** Where to go after login: same-site relative path from ?redirect=, else home. */
export function destination() {
  const r = getParam('redirect');
  const ok = r && r.startsWith('/') && !r.startsWith('//') && !r.includes('\\');
  return ok ? r : '/index.html';
}

export function skipIfLoggedIn() {
  if (isLoggedIn()) location.replace(destination());
}

/** Accepts either {token, user} or a flat {token, ...userFields} response. */
export function completeLogin(res) {
  const { token, user, ...rest } = res;
  saveSession({ token, user: user ?? rest });
  location.href = destination();
}