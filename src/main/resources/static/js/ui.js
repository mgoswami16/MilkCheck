import { CONFIG } from './config.js';
import { ICONS } from './icons.js';
import { ApiError, cartApi } from './api.js';
import { isLoggedIn, redirectToLogin } from './auth.js';

/* ---------- DOM and formatting helpers ---------- */

export const $  = (sel, root = document) => root.querySelector(sel);
export const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];
export const getParam = (name) => new URLSearchParams(location.search).get(name);

/** Escape anything that came from the API or the user before putting it in innerHTML. */
export function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (c) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

const money = new Intl.NumberFormat(CONFIG.LOCALE, { style: 'currency', currency: CONFIG.CURRENCY });
export const formatPrice = (n) => money.format(Number(n) || 0);

export const formatDate = (iso) =>
  iso ? new Date(iso).toLocaleDateString(CONFIG.LOCALE, { year: 'numeric', month: 'short', day: 'numeric' }) : '';

export const formatDateTime = (iso) =>
  iso ? new Date(iso).toLocaleString(CONFIG.LOCALE, { dateStyle: 'medium', timeStyle: 'short' }) : '';

export function debounce(fn, ms = 350) {
  let timer;
  return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), ms); };
}

/* ---------- Images: any failed <img> falls back to the placeholder ---------- */

document.addEventListener('error', (e) => {
  const el = e.target;
  if (el.tagName === 'IMG' && !el.dataset.fallback) {
    el.dataset.fallback = '1';
    el.src = CONFIG.PLACEHOLDER_IMG;
  }
}, true);

/* ---------- Toasts ---------- */

let toastRoot;

export function toast(message, type = 'info', duration = 3500) {
  if (!toastRoot) {
    toastRoot = document.createElement('div');
    toastRoot.className = 'toast-container';
    toastRoot.setAttribute('aria-live', 'polite');
    document.body.appendChild(toastRoot);
  }
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.setAttribute('role', type === 'error' ? 'alert' : 'status');
  el.innerHTML =
    `<span class="toast-icon">${ICONS[type] || ICONS.info}</span>` +
    `<span class="toast-msg"></span>` +
    `<button type="button" class="toast-close" aria-label="Dismiss">&times;</button>`;
  el.querySelector('.toast-msg').textContent = message;   // textContent = XSS-safe

  const dismiss = () => {
    el.classList.add('hide');
    el.addEventListener('animationend', () => el.remove(), { once: true });
  };
  el.querySelector('.toast-close').addEventListener('click', dismiss);
  toastRoot.appendChild(el);
  setTimeout(dismiss, duration);
}

export function showError(err) {
  let msg = 'Something went wrong. Please try again.';
  if (err instanceof ApiError) {
    msg = (err.fieldErrors && Object.values(err.fieldErrors)[0]) || err.message;
  }
  toast(msg, 'error', 5000);
}

/* ---------- Forms ---------- */

export function setLoading(btn, loading) {
  btn.classList.toggle('is-loading', loading);
  btn.disabled = loading;
  btn.setAttribute('aria-busy', String(loading));
}

export function clearFieldErrors(form) {
  $$('.input.invalid', form).forEach((el) => el.classList.remove('invalid'));
  $$('.field-error', form).forEach((el) => el.remove());
}

/** Maps the API's fieldErrors ({ email: "..." }) onto inputs by name. Returns true if any matched. */
export function applyFieldErrors(form, fieldErrors) {
  clearFieldErrors(form);
  let first = null;
  for (const [field, message] of Object.entries(fieldErrors || {})) {
    const input = form.elements[field];
    if (!input?.classList) continue;
    input.classList.add('invalid');
    const note = document.createElement('small');
    note.className = 'field-error';
    note.textContent = message;
    input.insertAdjacentElement('afterend', note);
    first ??= input;
  }
  first?.focus();
  return first !== null;
}

/* ---------- Cart badge and add-to-cart ---------- */

export function setCartBadge(count, animate = true) {
  const badge = document.getElementById('cartBadge');
  if (!badge) return;
  badge.textContent = count > 99 ? '99+' : String(count);
  badge.classList.toggle('show', count > 0);
  document.getElementById('cartLink')?.setAttribute('aria-label', `Shopping cart, ${count} item${count === 1 ? '' : 's'}`);
  if (animate) {
    badge.classList.remove('bump');
    void badge.offsetWidth;            // restart the animation
    badge.classList.add('bump');
  }
}

export async function addToCart(productId, quantity = 1, btn = null) {
  if (!isLoggedIn()) {
    toast('Please log in to add items to your cart', 'info');
    setTimeout(() => redirectToLogin(), 900);
    return false;
  }
  try {
    if (btn) setLoading(btn, true);
    const cart = await cartApi.add(productId, quantity);
    setCartBadge(cart.totalItems);
    toast('Added to cart', 'success');
    return true;
  } catch (err) {
    showError(err);
    return false;
  } finally {
    if (btn) setLoading(btn, false);
  }
}

/** Event delegation: call once per container; works for cards rendered later. */
export function bindAddToCart(root = document) {
  root.addEventListener('click', (e) => {
    const btn = e.target.closest('[data-add-to-cart]');
    if (btn && !btn.disabled) addToCart(Number(btn.dataset.addToCart), Number(btn.dataset.qty || 1), btn);
  });
}

/* ---------- Components ---------- */

export function productCard(p, i = 0) {
  const href = `/pages/product.html?id=${p.id}`;
  const soldOut = !p.inStock;
  const low = p.inStock && p.stockQuantity <= 10;
  const stockClass = soldOut ? 'stock--out' : low ? 'stock--low' : 'stock--in';
  const stockText = soldOut ? 'Out of stock' : low ? `Only ${p.stockQuantity} left` : 'In stock';

  return `
  <article class="product-card" style="--i:${i}">
    <a class="product-card__media" href="${href}">
      <img src="${escapeHtml(p.imageUrl || CONFIG.PLACEHOLDER_IMG)}" alt="${escapeHtml(p.name)}" loading="lazy" width="400" height="300">
      ${p.featured ? '<span class="ribbon">Featured</span>' : ''}
      ${soldOut ? '<span class="soldout-tag">Out of stock</span>' : ''}
    </a>
    <div class="product-card__body">
      <span class="product-card__category">${escapeHtml(p.categoryName)}</span>
      <h3 class="product-card__title"><a href="${href}">${escapeHtml(p.name)}</a></h3>
      <p class="product-card__desc">${escapeHtml(p.description || '')}</p>
      <p class="stock ${stockClass}">${stockText}</p>
      <div class="product-card__footer">
        <div class="price">${formatPrice(p.price)}${p.unit ? `<small>/ ${escapeHtml(p.unit)}</small>` : ''}</div>
        <button type="button" class="btn btn-success btn-sm" data-add-to-cart="${p.id}" ${soldOut ? 'disabled' : ''}>
          ${soldOut ? 'Sold out' : 'Add to cart'}
        </button>
      </div>
    </div>
  </article>`;
}

export function skeletonCards(n = 8) {
  const card = '<div class="skeleton-card"><div class="skeleton sk-img"></div><div class="skeleton sk-line"></div>' +
               '<div class="skeleton sk-line short"></div><div class="skeleton sk-line"></div></div>';
  return card.repeat(n);
}

/** `action` is trusted HTML supplied by the calling page (never user data). */
export function emptyState({ icon = 'milk', title, text = '', action = '' }) {
  return `
  <div class="empty-state">
    <div class="empty-state__icon">${ICONS[icon] || ICONS.milk}</div>
    <h3>${escapeHtml(title)}</h3>
    <p>${escapeHtml(text)}</p>
    ${action}
  </div>`;
}

/**
 * Renders numbered pagination into `container`.
 * `page` is 0-based (matches the API). `onChange(newPage)` fires on click.
 */
export function renderPagination(container, { page, totalPages }, onChange) {
  if (!totalPages || totalPages <= 1) { container.innerHTML = ''; return; }

  const pages = new Set([0, totalPages - 1, page - 1, page, page + 1]);
  const visible = [...pages].filter((p) => p >= 0 && p < totalPages).sort((a, b) => a - b);

  let html = `<button type="button" class="page-btn" data-page="${page - 1}" ${page === 0 ? 'disabled' : ''} aria-label="Previous page">&lsaquo;</button>`;
  visible.forEach((p, idx) => {
    if (idx > 0 && p - visible[idx - 1] > 1) html += '<span class="page-gap">&hellip;</span>';
    html += `<button type="button" class="page-btn ${p === page ? 'active' : ''}" data-page="${p}" ${p === page ? 'aria-current="page"' : ''}>${p + 1}</button>`;
  });
  html += `<button type="button" class="page-btn" data-page="${page + 1}" ${page >= totalPages - 1 ? 'disabled' : ''} aria-label="Next page">&rsaquo;</button>`;

  container.innerHTML = html;
  container.onclick = (e) => {
    const btn = e.target.closest('[data-page]');
    if (btn && !btn.disabled) onChange(Number(btn.dataset.page));
  };
}

/** Promise-based confirm modal. Resolves true on confirm, false on cancel/Esc/backdrop. */
export function confirmDialog({ title = 'Are you sure?', message = '', confirmText = 'Confirm', danger = false } = {}) {
  return new Promise((resolve) => {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';
    backdrop.innerHTML = `
      <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
        <h3 id="modalTitle"></h3>
        <p></p>
        <div class="modal-actions">
          <button type="button" class="btn btn-outline btn-sm" data-act="cancel">Cancel</button>
          <button type="button" class="btn ${danger ? 'btn-danger' : 'btn-primary'} btn-sm" data-act="ok"></button>
        </div>
      </div>`;
    backdrop.querySelector('h3').textContent = title;
    backdrop.querySelector('p').textContent = message;
    backdrop.querySelector('[data-act="ok"]').textContent = confirmText;

    const close = (result) => {
      document.removeEventListener('keydown', onKey);
      backdrop.classList.remove('show');
      setTimeout(() => backdrop.remove(), 150);
      resolve(result);
    };
    const onKey = (e) => { if (e.key === 'Escape') close(false); };

    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop) close(false);
      const act = e.target.closest('[data-act]')?.dataset.act;
      if (act) close(act === 'ok');
    });
    document.addEventListener('keydown', onKey);
    document.body.appendChild(backdrop);
    requestAnimationFrame(() => backdrop.classList.add('show'));
    backdrop.querySelector('[data-act="ok"]').focus();
  });
}

/* ---------- Status badges (orders and payments, used by customer and admin pages) ---------- */

const STATUS_CLASS = {
  PENDING: 'badge-warning', CONFIRMED: 'badge-info', PROCESSING: 'badge-info',
  SHIPPED: 'badge-primary', DELIVERED: 'badge-success', CANCELLED: 'badge-danger',
  PAID: 'badge-success', FAILED: 'badge-danger', REFUNDED: 'badge-muted'
};

export function statusBadge(status) {
  const label = String(status || '').charAt(0) + String(status || '').slice(1).toLowerCase();
  return `<span class="badge ${STATUS_CLASS[status] || 'badge-muted'}">${escapeHtml(label)}</span>`;
}