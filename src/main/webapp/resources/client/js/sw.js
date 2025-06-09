// sw.js
self.addEventListener('push', event => {
    const payload = event.data.json();
    const title = payload.title || "Thông báo mới";
    const options = {
        body: payload.message,
        icon: '/images/logo.png', // Đường dẫn đến icon
        data: { url: payload.url || '/' }
    };

    event.waitUntil(
        self.registration.showNotification(title, options)
    );
});

self.addEventListener('notificationclick', event => {
    event.notification.close();
    event.waitUntil(
        clients.openWindow(event.notification.data.url)
    );
});