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
import ProfileDropdown from "../profile/ProfileDropdown";
import { th } from "framer-motion/client";
import API_BASE_URL from "../../config";
import MarkerClusterGroup from 'react-leaflet-cluster';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()} />;
};

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

// Подгонка карты под маркеры
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

// Перемещение карты к маркеру
function FlyToLocation({ position, markerId, markerRefs, format }) {
    const map = useMap();
    React.useEffect(() => {
        if (position) {
            const zoom = format === "OFFLINE" ? 18 : 10;
            map.flyTo(position, zoom, { duration: 1.5 });
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

// Форматирование даты
const formatDateRange = (start, end) => {
    const options = { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" };
    const startStr = start.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    const endStr = end.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    return `${startStr} - ${endStr}`;
};

const Pagination = ({totalPages, currentPage, handlePageClick}) => {
    return(
        <div className={`pagination-controls ${totalPages < 2 ? "hidden" : ""}`}>
            <button
                className="back-forward"
                onClick={() => handlePageClick(currentPage - 1)}
                disabled={currentPage === 1}
            >
                &lt;
            </button>

            {/* Первая страница */}
            <button
                className="page-button"
                onClick={() => handlePageClick(1)}
                disabled={currentPage === 1}
            >
                1
            </button>

            {/* Многоточие после первой страницы */}
            {currentPage > 3 && <span className="pagination-ellipsis">...</span>}

            {/* Текущая - 1 */}
            {currentPage > 2 && (
                <button
                    className="page-button"
                    onClick={() => handlePageClick(currentPage - 1)}
                >
                    {currentPage - 1}
                </button>
            )}

            {/* Текущая страница */}
            {currentPage !== 1 && currentPage !== totalPages && (
                <button className="page-button active" disabled>
                    {currentPage}
                </button>
            )}

            {/* Текущая + 1 */}
            {currentPage < totalPages - 1 && (
                <button
                    className="page-button"
                    onClick={() => handlePageClick(currentPage + 1)}
                >
                    {currentPage + 1}
                </button>
            )}

            {/* Многоточие перед последней страницей */}
            {currentPage < totalPages - 2 && <span className="pagination-ellipsis">...</span>}

            {/* Последняя страница */}
            {totalPages > 1 && (
                <button
                    className="page-button"
                    onClick={() => handlePageClick(totalPages)}
                    disabled={currentPage === totalPages}
                >
                    {totalPages}
                </button>
            )}

            <button
                className="back-forward"
                onClick={() => handlePageClick(currentPage + 1)}
                disabled={currentPage === totalPages}
            >
                &gt;
            </button>
        </div>
    );
}

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
            selectedTags: [],
        };
    }

    componentDidMount() {
        this.checkAuth();
        this.loadEvents(1, this.state.search);
        this.loadTags();
    }
    // Загрузка тегов
    loadTags = () => {
        fetch(`${API_BASE_URL}/v1/tags`, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
        })
            .then((res) => res.json())
            .then((data) => {
                this.setState({ tags: data.list });
            })
            .catch((err) => console.error("Ошибка при загрузке тегов:", err));
    };

    // Загрузка мероприятий
    loadEvents = (page, search = "", searchTags = []) => {
        const { eventsPerPage } = this.state;
        const searchParam = search ? `&search=${encodeURIComponent(search)}` : "";
        const searchTagsParam = searchTags.length > 0 ? `&tags=${searchTags.join(",")}` : "";
        fetch(`${API_BASE_URL}/v1/events?page=${page}&pageSize=${eventsPerPage}${searchParam}${searchTagsParam}`)
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
    // Обработка изменения поискового запроса
    handleSearchChange = (e) => {
        const newSearch = e.target.value;
        this.setState({ search: newSearch });
    };
    // Обработка перехода на другую страницу
    handlePageClick = (pageNumber) => {
        this.loadEvents(pageNumber, this.state.search, this.state.selectedTags);
        const el = document.getElementsByClassName("left-panel")[0];
        el.scrollTo(0, 0);
    };

    // Поиск по тегу
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

    // Группировка событий по одинаковым координатам
    groupEventsByLocation(events) {
        const groups = new Map();
        for (const event of events) {
            const key = event.position.join(",");
            if (!groups.has(key)) groups.set(key, []);
            groups.get(key).push(event);
        }
        return groups;
    }
    // Получение маркера для события
    getMarkerIdForEvent(event) {
        const grouped = this.groupEventsByLocation(this.state.events);
        for (const [key, group] of grouped.entries()) {
            if (group.find((e) => e.id === event.id)) return key;
        }
        return null;
    }

    handleOutsideClick = (e) => {
        if (this.dropdownRef && !this.dropdownRef.contains(e.target)) {
            this.setState({ showDropdown: false });
        }
    };
    
    toggleDropdown = () => {
        this.setState((prevState) => ({ showDropdown: !prevState.showDropdown }));
    };

    handleMenuClick = (path) => {
        this.setState({ showDropdown: false });
        this.props.navigate(path);
    };

    checkAuth = () => {
        const { user, setUser } = this.context;

        // Если пользователь уже есть в контексте, пропускаем
        //if (user && user.id) return;

        fetch(`${API_BASE_URL}/auth/me`, {
            method: 'GET',
            credentials: 'include',
        })
        .then((res) => {
            if (!res.ok) throw new Error("Не авторизован");
            return res.json();
        })
        .then((data) => {
            const ctx = this.context;
            ctx.setUser({ 
                id: data.user.id,
                role: data.user.role,
                email: data.user.email,
                username: data.user.username,
                displayName: data.user.displayName,
                loggedIn: true,
                memberLastName: data.customUser.lastName,
                memberFirstName: data.customUser.firstName,
                memberPatronymic: data.customUser.patronymic,
                memberBirthDate: data.customUser.birthDate,
                memberBirthCity: data.customUser.birthCity,
                mebmerPrivacy: data.customUser.privacy,
                organizerName: data.customUser.name,
                organizerDescription: data.customUser.description,
                organizerIndustry: data.customUser.industry,
                organizerAddress: data.customUser.address,
                organizerAccredited: data.customUser.isAccredited,
                moderatorIsAdmin: data.customUser.isAdmin
                //token: data.token
              }); // сохраняем в context + localStorage
        })
        .catch((err) => {
            console.log("Ошибка авторизации:", err.message);
        });
    };

    render() {
        const { navigate } = this.props;
        const { events, tags, search, currentPage, eventsPerPage, totalEvents } = this.state;
        const totalPages = Math.ceil(totalEvents / eventsPerPage);
        const groupedEvents = this.groupEventsByLocation(events);

        console.log(this.context.user);

        return (
            <div className="events-container">
                <div className="header-bar">
                    <div className="top-logo" onClick={() => navigate("/events")} style={{ cursor: "pointer" }}>
                        <img src={EventHubLogo} alt="Logo" className="logo" />
                    </div>
                    <div className="login-button-container">
                        <ProfileDropdown navigate={navigate} />
                    </div>
                </div>

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
                                    if (e.key === "Enter") this.loadEvents(1, this.state.search);
                                }}
                            />
                            <button className="search-button-inside" onClick={() => this.loadEvents(1, this.state.search)} aria-label="Поиск">
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35M11 19a8 8 0 100-16 8 8 0 000 16z" />
                                </svg>
                            </button>
                        </div>
                        {/* Фильтр по тегам */}
                        <div className="tags-filter-wrapper">
                            {tags.map((tag) => {
                                const isSelected = this.state.selectedTags.includes(tag.name);
                                return (
                                    <button
                                        key={tag.name}
                                        onClick={() => this.toggleTag(tag.name)}
                                        className={`tag-button ${isSelected ? "selected" : ""}`}
                                    >
                                        {tag.name}
                                        {isSelected && <span className="remove-icon">×</span>}
                                    </button>
                                );
                            })}
                        </div>
                        {/* Верхняя пагинация */}
                        <Pagination totalPages={totalPages} currentPage={currentPage} handlePageClick={this.handlePageClick} />

                        {/* Карточки событий */}
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
                                        <button onClick={() => navigate(`/events/${event.id}`)} className="event-button details">
                                            Подробнее
                                        </button>
                                        <button
                                            onClick={() =>
                                                this.setState({
                                                    focusedEvent: event,
                                                    focusedMarkerId: this.getMarkerIdForEvent(event),
                                                })
                                            }
                                            className="event-button map"
                                        >
                                            Показать на карте
                                        </button>
                                    </div>
                                    <div className={`event-format ${event.format.toLowerCase()}`}>{event.format === "ONLINE" ? "Онлайн" : "Офлайн"}</div>
                                </div>
                            </motion.div>
                        ))}
                        {/* Нижняя пагинация */}
                        <Pagination totalPages={totalPages} currentPage={currentPage} handlePageClick={this.handlePageClick} />
                    </motion.div>
                    {/* Карта */}
                    <motion.div className="right-panel" initial={{ opacity: 0, x: 50 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.5 }}>
                        <MapContainer center={[55.75, 37.61]} zoom={11} minZoom={2} style={{ height: "100%" }} maxBounds={[[-90, -180],[90, 180]]}>
                            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap contributors" maxZoom={18} />
                            {events.length > 0 && <FitToAllMarkers events={events} />}
                            {this.state.focusedEvent && this.state.focusedMarkerId && (
                                <FlyToLocation
                                    position={this.state.focusedEvent.position}
                                    markerId={this.state.focusedMarkerId}
                                    markerRefs={this.markerRefs}
                                    format={this.state.focusedEvent.format}
                                />
                            )}

                            <MarkerClusterGroup
                                chunkedLoading
                                spiderfyOnMaxZoom={true}
                                showCoverageOnHover={false}
                                zoomToBoundsOnClick={true}
                                maxClusterRadius={60}
                                spiderfyDistanceMultiplier={1.5} // Расстояние между маркерами при раскрытии
                                iconCreateFunction={(cluster) => {
                                    // Кастомная иконка для кластера
                                    return leaflet.divIcon({
                                        html: `<span>${cluster.getChildCount()}</span>`,
                                        className: 'marker-cluster-custom',
                                        iconSize: leaflet.point(30, 30, true)
                                    });
                                }}
                            >
                                {[...groupedEvents.entries()].map(([key, group]) => {
                                    const [lat, lng] = key.split(",").map(Number);
                                    const icon = group[0].format === "ONLINE" ? onlineIcon : offlineIcon;
                                    const initialEventId = this.state.focusedEvent?.id;
                                    return (
                                        <Marker
                                            key={key}
                                            position={[lat, lng]}
                                            icon={icon}
                                            ref={(ref) => (this.markerRefs.current[key] = ref)}
                                            eventHandlers={{
                                                click: () => {
                                                    this.setState({
                                                        focusedEvent: group[0],
                                                        focusedMarkerId: key
                                                    });
                                                }
                                            }}
                                        >
                                            <Popup>
                                                <MultiEventPopup events={group} navigate={navigate} initialEventId={initialEventId}/>
                                            </Popup>
                                        </Marker>
                                    );
                                })}
                            </MarkerClusterGroup>
                        </MapContainer>
                    </motion.div>
                </div>
            </div>
        );
    }
}
{/* PopUp для сгрупированных событий */}
function MultiEventPopup({ events, navigate, initialEventId }) {
    const initialIndex = initialEventId ? events.findIndex(e => e.id === initialEventId) : 0;
    const [page, setPage] = React.useState(Math.max(0, initialIndex));
    const total = events.length;
    const event = events[page];
    React.useEffect(() => {
        if (initialEventId) {
            const index = events.findIndex(e => e.id === initialEventId);
            if (index >= 0) setPage(index);
        }
    }, [initialEventId, events]);
    return (
        <div>
            <strong>{event.title}</strong>
            <p>{event.shortDescription}</p>
            <p>{event.date}</p>
            <button onClick={() => navigate(`/events/${event.id}`)} className="event-button details">
                Подробнее
            </button>
            {total > 1 && (
                <div className="popup-pagination">
                    <button className="popup-page-button" onClick={() => setPage((p) => (p === 0 ? total - 1 : p - 1))}>{"<"}</button>
                        <span>
                            {page + 1} / {total}
                        </span>
                    <button className="popup-page-button" onClick={() => setPage((p) => (p + 1) % total)}>{">"}</button>
                </div>
            )}
        </div>
    );
}

export default withNavigation(EventsPage);
