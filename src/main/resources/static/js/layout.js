import { CONFIG } from './config.js';
import { ICONS, LOGO } from './icons.js';
import { isLoggedIn, isAdmin, getUser, logout } from './auth.js';
import { cartApi } from './api.js';
import { escapeHtml, setCartBadge, getParam } from './ui.js';

const initials = (name) =>
  String(name || '?').trim().split(/\s+/).slice(0, 2).map((w) => w[0]).join('').toUpperCase();

function headerHtml(active) {
  const user = isLoggedIn() ? getUser() : null;
  const fullName = user?.fullName || user?.name || 'User';
  const link = (key, href, label) =>
    `<a class="nav-link ${active === key ? 'active' : ''}" href="${href}">${label}</a>`;

  const account = user
    ? `<div class="user-menu">
         <button type="button" class="user-btn" id="userBtn" aria-haspopup="true" aria-expanded="false">
           <span class="avatar">${escapeHtml(initials(fullName))}</span>
           <span class="user-name">${escapeHtml(fullName.split(' ')[0])}</span>
           ${ICONS.chevron || ICONS.chevronDown || ''}
         </button>
         <div class="dropdown" id="userDropdown" role="menu">
           <div class="dropdown-head"><strong>${escapeHtml(fullName)}</strong><small>${escapeHtml(user.email || '')}</small></div>
           <a href="/pages/profile.html" role="menuitem">${ICONS.user} My profile</a>
           <a href="/pages/orders.html" role="menuitem">${ICONS.orders} My orders</a>
           ${isAdmin() ? `<a href="/admin/dashboard.html" role="menuitem">${ICONS.shield} Admin dashboard</a>` : ''}
           <hr>
           <button type="button" id="logoutBtn" role="menuitem">${ICONS.logout} Log out</button>
         </div>
       </div>`
    : `<a class="btn btn-ghost btn-sm" href="/pages/login.html">Log in</a>
       <a class="btn btn-primary btn-sm hide-mobile" href="/pages/register.html">Sign up</a>`;

  return `
  <a class="skip-link" href="#main">Skip to content</a>
  <header class="site-header" id="siteHeader">
    <div class="container header-inner">
      <a class="brand" href="/" aria-label="${CONFIG.BRAND.name} home">
        ${LOGO}<span class="brand-name">Milk<span>Check</span></span>
      </a>

      <nav class="main-nav" id="mainNav" aria-label="Main">
        ${link('home', '/', 'Home')}
        ${link('products', '/pages/products.html', 'Products')}
        <a class="nav-link" href="#contact">Contact</a>
        ${link('bulk', '/pages/bulk-enquire.html', 'Bulk Enquire')}
        <form class="header-search" id="headerSearch" role="search">
          ${ICONS.search}
          <input type="search" name="q" placeholder="Search dairy products..." aria-label="Search products" autocomplete="off" maxlength="80">
        </form>
      </nav>

      <div class="header-actions">
        <a class="icon-btn" id="cartLink" href="/pages/cart.html" aria-label="Shopping cart">
          ${ICONS.cart}<span class="cart-badge" id="cartBadge">0</span>
        </a>
        ${account}
        <button type="button" class="icon-btn nav-toggle" id="navToggle" aria-label="Toggle menu" aria-expanded="false" aria-controls="mainNav">${ICONS.menu}</button>
      </div>
    </div>
  </header>`;
}

function footerHtml() {
  const b = CONFIG.BRAND;
  const social = Object.entries(b.social)
    .map(([name, url]) =>
      `<a href="${escapeHtml(url)}" target="_blank" rel="noopener noreferrer" aria-label="${name}">${ICONS[name] || ''}</a>`)
    .join('');

  return `
  <footer class="site-footer">
    <div class="container">
      <div class="footer-grid">
        <div>
          <a class="brand brand--light" href="/">${LOGO}<span class="brand-name">Milk<span>Check</span></span></a>
          <p class="footer-about">${escapeHtml(b.tagline)}</p>
          <div class="social">${social}</div>
        </div>
        <div>
          <h4>Shop</h4>
          <ul class="footer-links">
            <li><a href="/pages/products.html">All products</a></li>
            <li><a href="/pages/products.html?sort=newest">New arrivals</a></li>
            <li><a href="/pages/bulk-enquire.html">Bulk Enquire</a></li>
            <li><a href="/pages/cart.html">Your cart</a></li>
          </ul>
        </div>
        <div>
          <h4>Account</h4>
          <ul class="footer-links">
            <li><a href="/pages/profile.html">My profile</a></li>
            <li><a href="/pages/orders.html">Order history</a></li>
            <li><a href="/pages/login.html">Log in</a></li>
          </ul>
        </div>
        <div id="contact">
          <h4>Contact us</h4>
          <ul class="footer-links footer-contact">
            <li>${ICONS.pin}<span>${escapeHtml(b.address)}</span></li>
            <li>${ICONS.phone}<a href="tel:${escapeHtml(b.phone.replace(/[^+\d]/g, ''))}">${escapeHtml(b.phone)}</a></li>
            <li>${ICONS.mail}<a href="mailto:${escapeHtml(b.email)}">${escapeHtml(b.email)}</a></li>
            <li>${ICONS.clock}<span>${escapeHtml(b.hours)}</span></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <span>&copy; ${new Date().getFullYear()} ${b.name}. All rights reserved.</span>
        <span>Fresh. Pure. Checked.</span>
      </div>
    </div>
  </footer>`;
}

function wireHeader() {
  const header = document.getElementById('siteHeader');
  const nav = document.getElementById('mainNav');
  const toggle = document.getElementById('navToggle');
  const userBtn = document.getElementById('userBtn');
  const dropdown = document.getElementById('userDropdown');

  const onScroll = () => header.classList.toggle('scrolled', window.scrollY > 4);
  onScroll();
  window.addEventListener('scroll', onScroll, { passive: true });

  const setNav = (open) => {
    nav.classList.toggle('open', open);
    toggle.setAttribute('aria-expanded', String(open));
    toggle.innerHTML = open ? ICONS.close : ICONS.menu;
  };
  toggle.addEventListener('click', () => setNav(!nav.classList.contains('open')));

  const setDropdown = (open) => {
    dropdown?.classList.toggle('open', open);
    userBtn?.setAttribute('aria-expanded', String(open));
  };
  userBtn?.addEventListener('click', (e) => {
    e.stopPropagation();
    setDropdown(!dropdown.classList.contains('open'));
  });

  document.addEventListener('click', (e) => {
    if (!e.target.closest('.user-menu')) setDropdown(false);
    if (!e.target.closest('#mainNav') && !e.target.closest('#navToggle')) setNav(false);
  });
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') { setDropdown(false); setNav(false); }
  });
  nav.addEventListener('click', (e) => { if (e.target.closest('a')) setNav(false); });

  document.getElementById('logoutBtn')?.addEventListener('click', () => logout('/'));

  const search = document.getElementById('headerSearch');
  const input = search.elements.q;
  input.value = getParam('keyword') || '';
  search.addEventListener('submit', (e) => {
    e.preventDefault();
    const q = input.value.trim();
    location.href = '/pages/products.html' + (q ? `?keyword=${encodeURIComponent(q)}` : '');
  });
}

async function loadCartBadge() {
  if (!isLoggedIn()) return;
  try {
    const cart = await cartApi.get({ redirect: false });
    setCartBadge(cart.totalItems, false);
  } catch { /* badge is non-critical; ignore */ }
}

export function initLayout() {
  const path = location.pathname;
  const inferred =
    path === '/' || path.endsWith('/index.html') ? 'home' :
    path.includes('/products') || path.includes('/product.html') ? 'products' :
    path.includes('/bulk-enquire') ? 'bulk' : '';
  const active = document.body.dataset.nav || inferred;

  let headerSlot = document.getElementById('site-header');
  if (!headerSlot) {
    headerSlot = document.createElement('div');
    headerSlot.id = 'site-header';
    document.body.prepend(headerSlot);
  }
  let footerSlot = document.getElementById('site-footer');
  if (!footerSlot) {
    footerSlot = document.createElement('div');
    footerSlot.id = 'site-footer';
    document.body.append(footerSlot);
  }

  headerSlot.innerHTML = headerHtml(active);
  wireHeader();
  loadCartBadge();
  footerSlot.innerHTML = footerHtml();
}

// Modules are deferred, so the DOM is parsed by the time this runs.
initLayout();