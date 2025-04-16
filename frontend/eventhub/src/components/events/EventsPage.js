import React, { Component } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import leaflet from "leaflet";
import { motion } from "framer-motion";
import onlineIconImg from "../../img/online-marker.png";
import offlineIconImg from "../../img/offline-marker.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import "leaflet/dist/leaflet.css";
import "../../css/EventsPage.css";
import UserContext from "../../UserContext";
import { useNavigate } from "react-router-dom";
import EventHubLogo from "../../img/eventhub.png";

// HOC для добавления navigate в класс-компонент
export const withNavigation = (WrappedComponent) => {
  return (props) => <WrappedComponent {...props} navigate={useNavigate()} />;
};

// Иконки
const onlineIcon = new leaflet.Icon({
  iconUrl: onlineIconImg,
  shadowUrl: iconShadow,
  iconSize: [41, 41],
  iconAnchor: [12, 41],
});

const offlineIcon = new leaflet.Icon({
  iconUrl: offlineIconImg,
  shadowUrl: iconShadow,
  iconSize: [41, 41],
  iconAnchor: [12, 41],
});


// Центрирование карты по всем точкам
function FitToAllMarkers({ events }) {
    const map = useMap();
    React.useEffect(() => {
        if (events.length > 0) {
            const bounds = leaflet.latLngBounds(events.map((e) => e.position));
            map.fitBounds(bounds, { padding: [30, 30] });
        }
    }, [events, map]);
    return null;
}

// перемещение карты к заданной точке
function FlyToLocation({ position, markerId, markerRefs, format }) {
    const map = useMap();
    React.useEffect(() => {
        if (position) {
            var zoom = format === "OFFLINE" ? 18 : 10;
            // переходим к заданному маркеру
            map.flyTo(position, zoom, {
                duration: 1.5
            });
            // открываем окошко этого маркера
            const marker = markerRefs.current[markerId];
            if (marker) {
                setTimeout(() => {
                    marker.openPopup();
                }, 1600);
            }
        }
    }, [position, map, markerId, markerRefs]);
    return null;
}

// Формат даты
const formatDateRange = (start, end) => {
  const options = { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" };
  const startStr = start.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
  const endStr = end.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
  return `${startStr} - ${endStr}`;
};

class EventsPage extends Component {
    static contextType = UserContext;

    constructor(props) {
        super(props);
        this.markerRefs = React.createRef();
        this.markerRefs.current = {};
        this.state = {
            events: [],
            search: "",
            focusedEvent: null,
            currentPage: 1,
            eventsPerPage: 10,
            totalEvents: 0,
            tags: [],
            selectedTags: [], // <--- добавлено
        };
    }

    componentDidMount() {
        this.loadEvents(1, this.state.search);

        this.loadTags();
    }
    loadTags = () => {
        fetch("http://localhost:9500/api/v1/tags", {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            })
            .then((res) => res.json())
            .then((data) => {
                this.setState({ tags: data.list });
            })
            .catch((err) => console.error("Ошибка при загрузке тегов:", err));
    }
    // Загрузка точек
    loadEvents = (page, search = "", searchTags=[]) => {
        const { eventsPerPage } = this.state;
        const searchParam = search ? `&search=${encodeURIComponent(search)}` : "";
        const searchTagsParam = searchTags.length > 0 ? `&tags=${searchTags.join(",")}` : "";
        fetch(`http://localhost:9500/api/v1/events?page=${page}&size=${eventsPerPage}${searchParam}${searchTagsParam}`)
            .then((res) => res.json())
            .then((data) => {
                const loadedEvents = data.list.map((e) => ({
                    id: e.id,
                    title: e.title,
                    shortDescription: e.shortDescription,
                    description: e.description,
                    format: e.format,
                    date: formatDateRange(e.startDateTime, e.endDateTime),
                    tags: e.tags?.map((tag) => tag.name) || [],
                    position: [e.latitude, e.longitude],
                    location: e.location,
                }));
                this.setState({
                    events: loadedEvents,
                    currentPage: page,
                    totalEvents: data.total || 0,
                });
            })
            .catch((err) => console.error("Ошибка при загрузке точек:", err));
    };
    // Обновляем значение поискового запроса
    handleSearchChange = (e) => {
        const newSearch = e.target.value;
        this.setState({ search: newSearch });
    };
    // Переключаем страницу
    handlePageClick = (pageNumber) => {
        this.loadEvents(pageNumber, this.state.search);
    };
    toggleTag = (tagName) => {
        this.setState((prevState) => {
            const isSelected = prevState.selectedTags.includes(tagName);
            const selectedTags = isSelected
                ? prevState.selectedTags.filter((t) => t !== tagName)
                : [...prevState.selectedTags, tagName];
            this.loadEvents(1, this.state.search, selectedTags);
            return { selectedTags };
        });
    };


    render() {
        const { navigate } = this.props;
        const { events, tags, search, currentPage, eventsPerPage, totalEvents } = this.state;
        const totalPages = Math.ceil(totalEvents / eventsPerPage);

        return (
            <div className="events-container">
                {/* Верхняя панель */}
                <div className="header-bar">
                    <div className="top-logo">
                        <img src={EventHubLogo} alt="Logo" className="logo" />
                    </div>
                    <div className="login-button-container">
                        <button onClick={() => navigate("/login")} className="login-button">
                            Войти
                        </button>
                    </div>
                </div>

                {/* Контент */}
                <div className="content-panels">
                    <motion.div className="left-panel" initial={{ opacity: 0, x: -50 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.5 }}>
                        {/* Поиск */}
                        <div className="search-wrapper">
                            <input
                                type="text"
                                placeholder="Поиск мероприятий"
                                className="search-input"
                                value={search}
                                onChange={this.handleSearchChange}
                                onKeyDown={(e) => {
                                    if (e.key === "Enter")
                                        this.loadEvents(1, this.state.search);
                                }}
                            />
                            <button
                                className="search-button-inside"
                                onClick={() => this.loadEvents(1, this.state.search)}
                                aria-label="Поиск"
                            >
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    width="20"
                                    height="20"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    stroke="currentColor"
                                >
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M21 21l-4.35-4.35M11 19a8 8 0 100-16 8 8 0 000 16z" />
                                </svg>
                            </button>
                        </div>
                        <div className="tags-filter-wrapper">
                            {tags.map((tag) => {
                                const isSelected = this.state.selectedTags.includes(tag.name);
                                return (
                                    <button
                                        key={tag.name}
                                        onClick={() => this.toggleTag(tag.name)}
                                        className={`tag-button ${isSelected ? 'selected' : ''}`}
                                    >
                                        {tag.name}
                                        {isSelected && <span className="remove-icon">×</span>}
                                    </button>
                                );
                            })}
                        </div>


                        {events.map((event) => (
                            <motion.div key={event.id} className="event-card" whileHover={{ scale: 1.02 }}>
                                <div className="event-date">{event.date}</div>
                                <h3 className="event-title">{event.title}</h3>
                                <p className="event-short-description">{event.shortDescription}</p>
                                <p className="event-location">{event.location}</p>
                                <div className="event-tags">
                                    {event.tags.map((tag, idx) => (
                                        <span key={idx} className="event-tag">{tag}</span>
                                    ))}
                                </div>
                                <div className="card-buttons">
                                    <div className="button-group">
                                        <button onClick={() => navigate(`/event/${event.id}`)} className="event-button details">
                                            Подробнее
                                        </button>
                                        <button onClick={() => this.setState({ focusedEvent: event })} className="event-button map">
                                            Показать на карте
                                        </button>
                                    </div>
                                    <div className={`event-format ${event.format.toLowerCase()}`}>
                                        {event.format === "ONLINE" ? "Онлайн" : "Офлайн"}
                                    </div>
                                </div>
                            </motion.div>
                        ))}

                        {/* Пагинация */}
                        <div className="pagination-controls">
                            {Array.from({ length: totalPages }, (_, i) => (
                                <button
                                    key={i}
                                    className='pagination-button'
                                    disabled={currentPage === i + 1}
                                    onClick={() => this.handlePageClick(i + 1)}
                                >
                                    {i + 1}
                                </button>
                            ))}
                        </div>
                    </motion.div>

                    {/* Карта */}
                    <motion.div className="right-panel" initial={{ opacity: 0, x: 50 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.5 }}>
                        <MapContainer center={[55.75, 37.61]} zoom={11} style={{ height: "100%" }}>
                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution="&copy; OpenStreetMap contributors"
                                maxZoom={18}
                            />
                            {events.length > 0 && <FitToAllMarkers events={events} />}
                            {this.state.focusedEvent && (
                                <FlyToLocation
                                    position={this.state.focusedEvent.position}
                                    markerId={this.state.focusedEvent.id}
                                    markerRefs={this.markerRefs}
                                    format={this.state.focusedEvent.format}
                                />
                            )}
                            {events.map((event) => (
                                <Marker
                                    key={event.id}
                                    position={event.position}
                                    icon={event.format === "ONLINE" ? onlineIcon : offlineIcon}
                                    ref={(ref) => {
                                        if (ref) this.markerRefs.current[event.id] = ref;
                                    }}
                                >
                                    <Popup>
                                        <strong>{event.title}</strong>
                                        <p>{event.shortDescription}</p>
                                        <p>{event.date}</p>
                                    </Popup>
                                </Marker>
                            ))}
                        </MapContainer>
                    </motion.div>
                </div>
            </div>
        );
    }
}


export default withNavigation(EventsPage);
