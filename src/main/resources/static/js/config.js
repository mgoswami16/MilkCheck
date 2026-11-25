export const CONFIG = Object.freeze({
  // '' = same origin (frontend served by Spring Boot).
  // If you host the frontend elsewhere, use e.g. 'http://localhost:8080'.
  API_BASE: '',

  CURRENCY: 'INR',          // change to 'INR', 'EUR', ... as needed
  LOCALE: 'en-IN',
  PLACEHOLDER_IMG: '/images/placeholder.svg',

  BRAND: {
    name: 'MilkCheck',
    tagline: 'Farm-fresh dairy, checked for quality and delivered to your door.',
    address: '24 Dairy Lane, Green Valley, CA 90210',
    phone: '+1 (555) 010-2025',
    email: 'hello@milkcheck.com',
    hours: 'Mon-Sat, 6:00 AM - 8:00 PM',
    social: {   // replace with your real profile URLs
      facebook: 'https://facebook.com',
      instagram: 'https://instagram.com',
      twitter: 'https://x.com',
      youtube: 'https://youtube.com'
    }
  }
});