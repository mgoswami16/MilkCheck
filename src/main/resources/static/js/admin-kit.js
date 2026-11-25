import { isLoggedIn, isAdmin, getUser, logout, redirectToLogin } from '/js/auth.js';
import { ICONS } from '/js/icons.js';
import { escapeHtml } from '/js/ui.js';

const NAV = [
  { key: 'dashboard', href: '/admin/dashboard.html', text: 'Dashboard', icon: ICONS.shield },
  { key: 'products',  href: '/admin/products.html',  text: 'Products',  icon: ICONS.milk },
  { key: 'orders',    href: '/admin/orders.html',    text: 'Orders',    icon: ICONS.orders },
  { key: 'customers', href: '/admin/customers.html', text: 'Customers', icon: ICONS.user },
  { key: 'enquiries', text: 'Bulk Enquiries', href: '/admin/enquiries.html', icon: ICONS.mail }
];

export const label = (s) => {
  const t = String(s ?? '');
  return t.charAt(0).toUpperCase() + t.slice(1).toLowerCase();
};

export const statusBadge = (s) => {
  const st = String(s || 'PENDING').toUpperCase();
  return `<span class="status status-${st.toLowerCase()}">${label(st)}</span>`;
};

export const orderCustomer = (o) =>
  o.customerName ?? o.userName ?? o.user?.name ?? o.customerEmail ?? o.userEmail ?? o.user?.email ?? '—';
export const orderTotal = (o) => Number(o.totalAmount ?? o.total ?? 0);
export const orderRef = (o) => o.orderNumber ?? o.id;

/** Guards the page and renders sidebar + top bar. Returns false if redirecting. */
export function initAdmin(active, title) {
  if (!isLoggedIn() || !isAdmin()) {
    redirectToLogin('Please log in as an administrator.');
    return false;
  }
  const user = getUser() || {};
  const side = document.getElementById('adminSidebar');
  const top = document.getElementById('adminTop');
  const shell = document.querySelector('.admin-shell');

  side.innerHTML = `
    <a class="admin-brand" href="/admin/dashboard.html">
      <img src="/images/logo.svg" alt="" width="34" height="34">
      <span>Milk<b>Check</b><small>Admin</small></span>
    </a>
    <nav class="admin-nav" aria-label="Admin">
      ${NAV.map(n => `
        <a href="${n.href}" class="${n.key === active ? 'active' : ''}" ${n.key === active ? 'aria-current="page"' : ''}>
          <span class="ni">${n.icon}</span>${n.text}
        </a>`).join('')}
    </nav>
    <div class="admin-side-foot"><a href="/index.html" target="_blank" rel="noopener">View store ↗</a></div>`;

  const name = user.name || user.email || 'Admin';
  top.innerHTML = `
    <button class="admin-toggle" id="adminToggle" aria-label="Open menu" aria-expanded="false">${ICONS.menu}</button>
    <h1 class="admin-title">${escapeHtml(title)}</h1>
    <div class="admin-user">
      <span class="admin-avatar">${escapeHtml(name.trim().charAt(0).toUpperCase())}</span>
      <span class="hide-mobile">${escapeHtml(name)}</span>
      <button class="btn btn-ghost btn-sm" id="adminLogout">Log out</button>
    </div>`;

  const overlay = document.createElement('div');
  overlay.className = 'admin-overlay';
  shell.appendChild(overlay);
  const toggle = document.getElementById('adminToggle');
  const setOpen = (open) => {
    side.classList.toggle('open', open);
    overlay.classList.toggle('show', open);
    toggle.setAttribute('aria-expanded', String(open));
  };
  toggle.addEventListener('click', () => setOpen(!side.classList.contains('open')));
  overlay.addEventListener('click', () => setOpen(false));
  document.getElementById('adminLogout').addEventListener('click', () => logout('/admin/login.html'));
  return true;
}

/* ---------- Modals ---------- */
export function showModal(bd) {
  bd.classList.add('show');
  bd._onKey = (e) => { if (e.key === 'Escape') hideModal(bd); };
  document.addEventListener('keydown', bd._onKey);
  bd.onclick = (e) => { if (e.target === bd) hideModal(bd); };
}
export function hideModal(bd) {
  bd.classList.remove('show');
  if (bd._onKey) document.removeEventListener('keydown', bd._onKey);
}

/** Resolves true if confirmed. Text is escaped. */
export function confirmDialog({ title, message, confirmText = 'Confirm', cancelText = 'Cancel', danger = false }) {
  return new Promise((resolve) => {
    const bd = document.createElement('div');
    bd.className = 'modal-backdrop';
    bd.innerHTML = `
      <div class="modal" role="dialog" aria-modal="true" aria-labelledby="cd-title">
        <h3 id="cd-title">${escapeHtml(title)}</h3>
        <p>${escapeHtml(message)}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" data-r="0">${escapeHtml(cancelText)}</button>
          <button class="btn ${danger ? 'btn-danger' : 'btn-primary'}" data-r="1">${escapeHtml(confirmText)}</button>
        </div>
      </div>`;
    document.body.appendChild(bd);
    requestAnimationFrame(() => bd.classList.add('show'));
    const done = (v) => {
      document.removeEventListener('keydown', onKey);
      bd.classList.remove('show');
      setTimeout(() => bd.remove(), 200);
      resolve(v);
    };
    const onKey = (e) => { if (e.key === 'Escape') done(false); };
    document.addEventListener('keydown', onKey);
    bd.addEventListener('click', (e) => {
      if (e.target === bd) return done(false);
      const b = e.target.closest('[data-r]');
      if (b) done(b.dataset.r === '1');
    });
    bd.querySelector('[data-r="0"]').focus();
  });
}

/* ---------- Pager ---------- */
export function renderPager(el, page, totalPages) {
  if (!totalPages || totalPages <= 1) { el.innerHTML = ''; return; }
  const set = new Set([0, totalPages - 1, page - 1, page, page + 1]);
  const nums = [...set].filter(n => n >= 0 && n < totalPages).sort((a, b) => a - b);
  let html = `<button class="page-btn" data-page="${page - 1}" ${page === 0 ? 'disabled' : ''} aria-label="Previous page">‹</button>`;
  let prev = -1;
  for (const n of nums) {
    if (n - prev > 1) html += '<span class="page-gap">…</span>';
    html += `<button class="page-btn ${n === page ? 'active' : ''}" data-page="${n}" ${n === page ? 'aria-current="page"' : ''}>${n + 1}</button>`;
    prev = n;
  }
  html += `<button class="page-btn" data-page="${page + 1}" ${page >= totalPages - 1 ? 'disabled' : ''} aria-label="Next page">›</button>`;
  el.innerHTML = html;
}

export function bindPager(el, onPage) {
  el.addEventListener('click', (e) => {
    const b = e.target.closest('[data-page]');
    if (!b || b.disabled) return;
    onPage(Number(b.dataset.page));
  });
}