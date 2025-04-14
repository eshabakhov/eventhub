import React from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import leaflet from "leaflet";
import { motion } from "framer-motion";
import onlineIconImg from "../../img/online-marker.png";
import offlineIconImg from "../../img/offline-marker.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import "leaflet/dist/leaflet.css";
import "../../css/EventsPage.css";
import UserContext from "../../UserContext";

// метка для онлайн мероприятий
const onlineIcon = new leaflet.Icon({
    iconUrl: onlineIconImg,
    shadowUrl: iconShadow,
    iconSize: [41, 41],
    iconAnchor: [12, 41],
});

// метка для оффлайн мероприятий
const offlineIcon = new leaflet.Icon({
    iconUrl: offlineIconImg,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
});

// масштабирование и центрирование карты,
// чтобы все метки мероприятий поместились в область видимости карты
function FitToAllMarkers({ events }) {
    const map = useMap();
    React.useEffect(() => {
        if (events.length > 0) {
            const bounds = leaflet.latLngBounds(events.map(e => e.position));
            map.fitBounds(bounds, { padding: [30, 30] });
        }
    }, [events, map]);
    return null;
}

// перемещение карты к заданной точке
function FlyToLocation({ position }) {
    const map = useMap();
    React.useEffect(() => {
        if (position) {
            map.flyTo(position, 13, {
                duration: 1.5
            });
        }
    }, [position, map]);
    return null;
}

// форматирование даты начала и даты окончания мер
const formatDateRange = (start, end) => {
    const options = { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' };
    const startStr = start.toLocaleString('ru-RU', options).replace(',', '').replaceAll('/', '.');
    const endStr = end.toLocaleString('ru-RU', options).replace(',', '').replaceAll('/', '.');
    return `${startStr} - ${endStr}`;
};

class EventsPage extends React.Component {
    static contextType = UserContext;
    constructor(props) {
        super(props);
        this.state = {
            events: [],
            search: "",
            focusedEvent: null
        };
        this.navigate = null;
    }

    componentDidMount() {
        fetch("http://localhost:9500/api/v1/events")
            .then((res) => res.json())
            .then((data) => {
                const loadedEvents = data.list.map((e) => ({
                    id: e.id,
                    title: e.title,
                    description: e.description,
                    format: e.format,
                    date: formatDateRange(e.startDateTime, e.endDateTime),
                    tags: e.tags?.map(tag => tag.name) || [],
                    position: [e.latitude, e.longitude],
                    location: e.location
                }));
                this.setState({ events: loadedEvents });
            })
            .catch((err) => console.error("Ошибка при загрузке точек:", err));
    }

    render() {
        // Задаем контекст пользователя
        const { user } = this.context;
        return (
            <div className="events-container">
                <motion.div
                    className="events-list"
                    initial={{ opacity: 0, x: -50 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.5 }}
                >
                    <input
                    type="text"
                    placeholder="Поиск мероприятий"
                    className="search-input"
                    value={this.state.search}
                    onChange={(e) => this.setState({ search: e.target.value })}
                    />
                    {this.state.events
                    .filter((event) =>
                        event.title.toLowerCase().includes(this.state.search.toLowerCase()) ||
                        event.tags.some((tag) =>
                        tag.toLowerCase().includes(this.state.search.toLowerCase())
                        )
                    )
                    .map((event) => (
                        <motion.div
                        key={event.id}
                        className="event-card"
                        whileHover={{ scale: 1.02 }}
                        >
                        <div className="event-date">{event.date}</div>
                        <h3 className="event-title">{event.title}</h3>
                        <p className="event-description">{event.description}</p>
                        <p className="event-location">{event.location}</p>
                        <div className="event-tags">
                            {event.tags.map((tag, idx) => (
                            <span key={idx} className="event-tag">{tag}</span>
                            ))}
                        </div>
                        <div className="card-buttons">
                            <div className="button-group">
                            <button
                                onClick={() => this.navigate(`/event/${event.id}`)}
                                className="event-button details"
                            >
                                Подробнее
                            </button>
                            <button
                                onClick={() => this.setState({ focusedEvent: event })}
                                className="event-button map"
                            >
                                Показать на карте
                            </button>
                            </div>
                            <div className="event-format">
                            {event.format === "ONLINE" ? "Онлайн" : "Оффлайн"}
                            </div>
                        </div>
                        </motion.div>
                    ))}
                </motion.div>

                <motion.div
                    className="map-container"
                    initial={{ opacity: 0, x: 50 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.5 }}
                >
                    <MapContainer center={[55.75, 37.61]} zoom={11} style={{ height: "100%" }}>
                    <TileLayer
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        attribution="&copy; OpenStreetMap contributors"
                        maxZoom={18}
                    />
                    {this.state.events.length > 0 && <FitToAllMarkers events={this.state.events} />}
                    {this.state.focusedEvent && <FlyToLocation position={this.state.focusedEvent.position} />}
                    {this.state.events.map((event) => (
                        <Marker
                        key={event.id}
                        position={event.position}
                        icon={event.format === "ONLINE" ? onlineIcon : offlineIcon}
                        >
                        <Popup>
                            <strong>{event.title}</strong>
                            <p>{event.description}</p>
                            <p>{event.date}</p>
                        </Popup>
                        </Marker>
                    ))}
                    </MapContainer>
                </motion.div>
                </div>
        );
    }

    // componentDidUpdate() {
    //     if (!this.navigate) {
    //         this.navigate = require("react-router-dom").useNavigate();
    //     }
    // }
}

export default EventsPage;
