import API_BASE_URL from "../config";

const map = L.map('map').setView([55.75, 37.61], 10);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap',
        maxZoom: 18,
    }).addTo(map);

    fetch(`${API_BASE_URL}/v1/events`)
        .then(res => res.json())
        .then(data => {
            const bounds = [];

            data.list.forEach(point => {
                const marker = L.marker([point.latitude, point.longitude])
                    .addTo(map)
                    .bindTooltip(point.title || 'Без названия', {
                        permanent: false,
                        direction: 'top',
                    });

                bounds.push([point.latitude, point.longitude]);
            });

            if (bounds.length > 0) {
                map.fitBounds(bounds, { padding: [30, 30] });
            }
        })
        .catch(err => console.error('Ошибка при загрузке точек:', err));

    const element = document.querySelector('.leaflet-bottom.leaflet-right');
    if (element) {
        element.remove();
    }