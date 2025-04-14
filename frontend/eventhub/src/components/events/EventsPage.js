import React from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import { motion } from "framer-motion";
import onlineIconImg from "../../img/online-marker.png";
import offlineIconImg from "../../img/offline-marker.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import "leaflet/dist/leaflet.css";
import "../../css/EventsPage.css";

const onlineIcon = new L.Icon({
    iconUrl: onlineIconImg,
    shadowUrl: iconShadow,
    iconSize: [41, 41],
    iconAnchor: [12, 41],
});

const offlineIcon = new L.Icon({
    iconUrl: offlineIconImg,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
});

function FitToAllMarkers({ events }) {
    const map = useMap();
    React.useEffect(() => {
        if (events.length > 0) {
            const bounds = L.latLngBounds(events.map(e => e.position));
            map.fitBounds(bounds, { padding: [30, 30] });
        }
    }, [events, map]);
    return null;
}

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

const formatDateRange = (start, end) => {
    const options = { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' };
    const startStr = start.toLocaleString('ru-RU', options).replace(',', '').replaceAll('/', '.');
    const endStr = end.toLocaleString('ru-RU', options).replace(',', '').replaceAll('/', '.');
    return `${startStr} - ${endStr}`;
};

class EventsPage extends React.Component {
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
        return (
            <div className="flex h-screen">
                <motion.div
                    className="w-1/2 p-4 overflow-y-scroll"
                    initial={{ opacity: 0, x: -50 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.5 }}
                >
                    <input
                        type="text"
                        placeholder="Поиск мероприятий"
                        className="w-full p-2 mb-4 border rounded"
                        value={this.state.search}
                        onChange={(e) => this.setState({ search: e.target.value })}
                    />
                    {this.state.events
                        .filter((event) =>
                            event.title.toLowerCase().includes(this.state.search.toLowerCase()) ||
                            event.tags.some((tag) => tag.toLowerCase().includes(this.state.search.toLowerCase()))
                        )
                        .map((event) => (
                            <motion.div
                                key={event.id}
                                className="relative p-4 mb-4 border rounded shadow-sm"
                                whileHover={{ scale: 1.02 }}
                            >
                                <div className="absolute top-2 right-2 text-xs text-gray-500">{event.date}</div>
                                <h3 className="text-lg font-bold">{event.title}</h3>
                                <p className="text-sm text-gray-600">{event.description}</p>
                                <p className="text-sm text-gray-500 italic">{event.location}</p>
                                <div className="flex flex-wrap gap-2 mt-2">
                                    {event.tags.map((tag, idx) => (
                                        <span
                                            key={idx}
                                            className="px-2 py-1 text-xs bg-gray-200 rounded"
                                        >
                                            {tag}
                                        </span>
                                    ))}
                                </div>
                                <div className="flex justify-between items-center mt-2">
                                    <div className="space-x-2">
                                        <button
                                            onClick={() => this.navigate(`/event/${event.id}`)}
                                            className="px-2 py-1 text-sm bg-blue-500 text-white rounded"
                                        >
                                            Подробнее
                                        </button>
                                        <button
                                            onClick={() => this.setState({ focusedEvent: event })}
                                            className="px-2 py-1 text-sm bg-green-500 text-white rounded"
                                        >
                                            Показать на карте
                                        </button>
                                    </div>
                                    <div className="text-xs text-gray-500 italic">
                                        {event.format === "ONLINE" ? "Онлайн" : "Оффлайн"}
                                    </div>
                                </div>
                            </motion.div>
                        ))}
                </motion.div>

                <motion.div
                    className="w-1/2"
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
