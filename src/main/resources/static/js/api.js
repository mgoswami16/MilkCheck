import { CONFIG } from './config.js';
import { getToken, clearSession, redirectToLogin } from './auth.js';

export class ApiError extends Error {
  constructor(status, message, fieldErrors = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

function buildUrl(path, params) {
  const url = new URL(CONFIG.API_BASE + path, window.location.origin);
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v);
  });
  return url;
}

/**
 * opts.auth     (default true)  attach the JWT when one exists. Use false for login/register.
 * opts.redirect (default true)  on 401 with a stored token, clear it and go to the login page.
 */
async function request(method, path, { body, params, auth = true, redirect = true } = {}) {
  const headers = { Accept: 'application/json' };
  const token = getToken();
  if (auth && token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let res;
  try {
    res = await fetch(buildUrl(path, params), {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined
    });
  } catch {
    throw new ApiError(0, 'Unable to reach the server. Please check your connection and try again.');
  }

  if (res.status === 401 && auth && token) {
    clearSession();
    if (redirect) redirectToLogin('expired');
    throw new ApiError(401, 'Your session has expired. Please log in again.');
  }

  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new ApiError(res.status, data?.message || `Request failed (${res.status})`, data?.fieldErrors || null);
  }
  return data;
}

export const api = {
  get:    (path, params, opts) => request('GET', path, { params, ...opts }),
  post:   (path, body, opts)   => request('POST', path, { body, ...opts }),
  put:    (path, body, opts)   => request('PUT', path, { body, ...opts }),
  patch:  (path, body, opts)   => request('PATCH', path, { body, ...opts }),
  delete: (path, opts)         => request('DELETE', path, { ...opts })
};

/* ---------- Endpoint helpers (mirror the Phase 4 API reference) ---------- */

export const authApi = {
  register:   (data) => api.post('/api/auth/register', data, { auth: false }),
  login:      (data) => api.post('/api/auth/login', data, { auth: false }),
  adminLogin: (data) => api.post('/api/auth/admin/login', data, { auth: false })
};

export const catalogApi = {
  categories: ()       => api.get('/api/categories'),
  products:   (params) => api.get('/api/products', params),
  featured:   ()       => api.get('/api/products/featured'),
  product:    (id)     => api.get(`/api/products/${id}`)
};

export const cartApi = {
  get:    (opts)               => api.get('/api/cart', null, opts),
  add:    (productId, quantity) => api.post('/api/cart/items', { productId, quantity }),
  update: (itemId, quantity)   => api.put(`/api/cart/items/${itemId}`, { quantity }),
  remove: (itemId)             => api.delete(`/api/cart/items/${itemId}`),
  clear:  ()                   => api.delete('/api/cart')
};

export const orderApi = {
  checkout: (data) => api.post('/api/orders', data),
  list:     ()     => api.get('/api/orders'),
  get:      (id)   => api.get(`/api/orders/${id}`),
  cancel:   (id)   => api.patch(`/api/orders/${id}/cancel`)
};

export const userApi = {
  profile:        ()     => api.get('/api/users/me'),
  update:         (data) => api.put('/api/users/me', data),
  changePassword: (data) => api.put('/api/users/me/password', data)
};
export const enquiryApi = {
  submit: (data) => api.post('/api/bulk-enquiries', data),
};
export const adminApi = {
  dashboard:         ()            => api.get('/api/admin/dashboard'),
  products:          (params)      => api.get('/api/admin/products', params),
  product:           (id)          => api.get(`/api/admin/products/${id}`),
  createProduct:     (data)        => api.post('/api/admin/products', data),
  updateProduct:     (id, data)    => api.put(`/api/admin/products/${id}`, data),
  deleteProduct:     (id)          => api.delete(`/api/admin/products/${id}`),
  orders:            (params)      => api.get('/api/admin/orders', params),
  order:             (id)          => api.get(`/api/admin/orders/${id}`),
  updateOrderStatus: (id, status)  => api.patch(`/api/admin/orders/${id}/status`, { status }),
  customers:         (params)      => api.get('/api/admin/customers', params),
  setCustomerStatus: (id, enabled) => api.patch(`/api/admin/customers/${id}/status`, { enabled }),
  enquiries:         (params)      => api.get('/api/admin/enquiries', params),
 updateEnquiryStatus: (id, status) => api.patch(`/api/admin/enquiries/${id}/status`, { status })
};