// Service worker de Bolsillo.
// Nota: dentro del WebView nativo de Capacitor los archivos ya viajan embebidos
// en el APK/IPA, así que este SW es sobre todo útil si en algún momento sirves
// la misma carpeta "www" como PWA desde un dominio. En index.html el registro
// está protegido con `if (!window.Capacitor)` para no interferir con las
// actualizaciones de la app nativa (evita servir una versión cacheada vieja
// tras instalar una actualización del APK).

const CACHE_NAME = 'bolsillo-cache-v2';
const CORE_ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png',
  './assets/logo-mi-bolsillo.gif'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(CORE_ASSETS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    ).then(() => self.clients.claim())
  );
});

// Estrategia: network-first para el HTML (para no quedarte con una versión
// vieja de la app), cache-first para el resto de assets estáticos.
self.addEventListener('fetch', (event) => {
  const { request } = event;
  if (request.method !== 'GET') return;

  const isHTML = request.mode === 'navigate' || request.destination === 'document';

  if (isHTML) {
    event.respondWith(
      fetch(request)
        .then((res) => {
          const copy = res.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
          return res;
        })
        .catch(() => caches.match(request).then((r) => r || caches.match('./index.html')))
    );
    return;
  }

  event.respondWith(
    caches.match(request).then((cached) => {
      if (cached) return cached;
      return fetch(request).then((res) => {
        const copy = res.clone();
        caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
        return res;
      });
    })
  );
});
