import { CONFIG } from './config.js';
import { getToken } from './auth.js';

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_BYTES = 5 * 1024 * 1024;

/** Uploads one image to the admin endpoint and resolves with its URL path. */
export async function uploadProductImage(file) {
  const body = new FormData();
  body.append('file', file);

  const res = await fetch(`${CONFIG.API_BASE}/api/admin/uploads/products`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${getToken()}` },
    body
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.message || 'Image upload failed');
  }
  return data.url;
}

/**
 * Wires up the product image section.
 * Returns { setExisting(url), reset() } for edit and reset flows.
 */
export function initImageUpload({ input, preview, hiddenUrl, status, removeBtn, hint }) {
  const setStatus = (text, kind = '') => {
    status.textContent = text;
    status.dataset.kind = kind;
  };

  const showPreview = (src) => {
    preview.src = src;
    preview.hidden = false;
    if (hint) hint.hidden = true;
    removeBtn.hidden = false;
  };

  const clear = () => {
    input.value = '';
    hiddenUrl.value = '';
    preview.removeAttribute('src');
    preview.hidden = true;
    if (hint) hint.hidden = false;
    removeBtn.hidden = true;
    setStatus('');
  };

  input.addEventListener('change', async () => {
    const file = input.files?.[0];
    if (!file) return;

    if (!ALLOWED_TYPES.includes(file.type)) {
      input.value = '';
      setStatus('Only JPG, PNG or WEBP images are allowed.', 'error');
      return;
    }
    if (file.size > MAX_BYTES) {
      input.value = '';
      setStatus('Image must be 5 MB or smaller.', 'error');
      return;
    }

    showPreview(URL.createObjectURL(file));
    setStatus('Uploading…', 'pending');
    removeBtn.disabled = true;

    try {
      hiddenUrl.value = await uploadProductImage(file);
      setStatus('Image uploaded', 'ok');
    } catch (err) {
      hiddenUrl.value = '';
      setStatus(err.message, 'error');
    } finally {
      removeBtn.disabled = false;
    }
  });

  removeBtn.addEventListener('click', clear);

  return {
    setExisting(url) {
      if (url) {
        hiddenUrl.value = url;
        showPreview(url.startsWith('http') ? url : `${CONFIG.API_BASE}${url}`);
        setStatus('Current image', 'ok');
      } else {
        clear();
      }
    },
    reset: clear
  };
}