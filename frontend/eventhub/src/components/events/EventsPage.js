import React, {Component} from "react";
import {MapContainer, TileLayer, Marker, Popup, useMap} from "react-leaflet";
import leaflet from "leaflet";
import {motion} from "framer-motion";
import onlineIconImg from "../../img/online-marker.png";
import offlineIconImg from "../../img/offline-marker.png";
import onlineIconStarImg from "../../img/star-green.png";
import offlineIconStarImg from "../../img/star-red.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import "leaflet/dist/leaflet.css";
import "../../css/EventsPage.css";
import UserContext from "../../UserContext";
import {useNavigate} from "react-router-dom";
import API_BASE_URL from "../../config";
import MarkerClusterGroup from 'react-leaflet-cluster';
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import Pagination from "../common/Pagination";
import CurrentUser from "../common/CurrentUser";
import defaultEventImage from "../../img/image-512.png";

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
};

const onlineIcon = new leaflet.Icon({
    iconUrl: onlineIconImg,
    shadowUrl: iconShadow,
    iconSize: [41, 41],
    iconAnchor: [12, 41],
});

const onlineIconStar = new leaflet.Icon({
    iconUrl: onlineIconStarImg,
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
const offlineIconStar = new leaflet.Icon({
    iconUrl: offlineIconStarImg,
    shadowUrl: iconShadow,
    iconSize: [41, 41],
    iconAnchor: [12, 41],
});




const FavoriteStar = ({ tag, userId, isFavorite, toggleFavorite }) => {
    const handleClick = (e) => {
        e.stopPropagation(); // Prevent triggering the tag click
        toggleFavorite(tag.id, userId, !isFavorite);
    };

    return (
        <div className="star-container" onClick={handleClick}>
            <svg
                className={`star-icon ${isFavorite ? 'favorite' : ''}`}
                viewBox="0 0 24 24"
            >
                <path
                    d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                />
            </svg>
        </div>
    );
};

// Подгонка карты под маркеры
function FitToAllMarkers({events}) {
    const map = useMap();
    React.useEffect(() => {
        if (events.length > 0) {
            const bounds = leaflet.latLngBounds(events.map((e) => e.position));
            map.fitBounds(bounds, {padding: [30, 30]});
        }
    }, [events, map]);
    return null;
}

// Перемещение карты к маркеру
function FlyToLocation({position, markerId, markerRefs, format}) {
    const map = useMap();
    React.useEffect(() => {
        if (position) {
            const zoom = format === "OFFLINE" ? 18 : 10;
            map.flyTo(position, zoom, {duration: 1.5});
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
    const options = {day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit"};
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
            recommendations: [],
            search: "",
            focusedEvent: null,
            currentPage: 1,
            eventsPerPage: 10,
            totalEvents: 0,
            tags: [],
            selectedTags: [],
            sidebarOpen: false,
            activeTab: "allEvents", // Новая вкладка для переключения
            latitude: null,
            longitude: null
        };
    }

    sidebarRef = React.createRef();

    componentDidMount() {
        CurrentUser.fetchCurrentUser(this.context.setUser);
        this.loadEvents(1, this.state.search);
        //this.loadRecommendations(1, this.state.search);
        this.loadTags();
        document.addEventListener("mousedown", this.handleClickOutside);

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const { latitude, longitude } = position.coords;
                    this.setState({
                        latitude,
                        longitude,
                    }, () => {
                        console.log(`Geolocation success: Latitude ${latitude}, Longitude ${longitude}`);
                    });
                },
                (error) => {
                    console.warn(`Geolocation error (${error.code}): ${error.message}`);
                    this.setState({
                        latitude: 55.75,
                        longitude: 37.61,
                    });
                },
                {
                    enableHighAccuracy: true,
                    timeout: 5000,
                    maximumAge: 0
                }
            );
        } else {
            console.warn("Geolocation not supported by browser");
            this.setState({
                latitude: 55.75,
                longitude: 37.61,
            });
        }


    }

    componentWillUnmount() {
        document.removeEventListener("mousedown", this.handleClickOutside);
    }

    toggleSidebar = () => {
        this.setState(prev => ({sidebarOpen: !prev.sidebarOpen}));
    };

    handleClickOutside = (event) => {
        if (this.state.sidebarOpen &&
            this.sidebarRef.current &&
            !this.sidebarRef.current.contains(event.target) &&
            !event.target.classList.contains('burger-button') &&
            !event.target.closest('.burger-button')) {
            this.setState({sidebarOpen: false});
        }
    };
    // Загрузка тегов
    loadTags = () => {
        fetch(`${API_BASE_URL}/v1/tags`, {
            method: "GET",
            headers: {"Content-Type": "application/json"},
            credentials: "include",
        })
            .then((res) => res.json())
            .then((data) => {
                this.setState({tags: data.list});
                this.loadFavoriteTags();
            })
            .catch((err) => console.error("Ошибка при загрузке тегов:", err));
    };
    // Загрузка избранных тегов
    loadFavoriteTags = () => {
        const currentUser = this.context.user;
        if (!currentUser || !currentUser.id) return;

        fetch(`${API_BASE_URL}/v1/tags/${currentUser.id}`, {
            method: "GET",
            headers: {"Content-Type": "application/json"},
            credentials: "include",
        })
            .then((res) => res.json())
            .then((data) => {
                this.setState(prevState => ({
                    tags: prevState.tags.map(tag => ({
                        ...tag,
                        isFavorite: data.list.some(favTag => favTag.id === tag.id)
                    }))
                }));
            })
            .catch((err) => console.error("Ошибка при загрузке избранных тегов:", err));
    };

    // Добавление/удаление тега в избранное
    toggleFavorite = async (tagId, userId, isFavorite) => {
        try {
            const method = isFavorite ? 'POST' : 'DELETE';
            const response = await fetch(`${API_BASE_URL}/v1/tags/${tagId}/users/${userId}`, {
                method,
                headers: { "Content-Type": "application/json" },
                credentials: "include",
            });

            if (response.ok) {
                this.setState(prevState => ({
                    tags: prevState.tags.map(tag =>
                        tag.id === tagId ? { ...tag, isFavorite: isFavorite } : tag
                    )
                }));
            }
        } catch (err) {
            console.error("Ошибка добавления тега в избранное", err);
        }
    };

    // Загрузка мероприятий
    loadEvents = (page, search = "", searchTags = []) => {
        const {eventsPerPage} = this.state;
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
                    imageUrl: e.pictures || null  // <-- добавлено
                }));
                this.setState({
                    events: loadedEvents,
                    currentPage: page,
                    totalEvents: data.total || 0,
                });
            })
            .catch((err) => console.error("Ошибка при загрузке точек:", err));
    };

    loadRecommendations = (page, search = "", searchTags = []) => {
        const { eventsPerPage } = this.state;
        const currentUser = this.context.user;
        if (!currentUser) return;
        const searchParam = search ? `&search=${encodeURIComponent(search)}` : "";
        const searchTagsParam = searchTags.length > 0 ? `&tags=${searchTags.join(",")}` : "";
        const url = `${API_BASE_URL}/v1/recommend?lat=${this.state.latitude}&lon=${this.state.longitude}&page=${page}&pageSize=${eventsPerPage}${searchParam}${searchTagsParam}`;
        fetch(url, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
        })
            .then((res) => res.json())
            .then((data) => {
                const loadedRecommendations = data.list.map((e) => ({
                    id: e.id,
                    title: e.title,
                    shortDescription: e.shortDescription,
                    description: e.description,
                    format: e.format,
                    date: formatDateRange(e.startDateTime, e.endDateTime),
                    tags: e.tags?.map((tag) => tag.name) || [],
                    position: [e.latitude, e.longitude],
                    location: e.location,
                    imageUrl: e.pictures || null  // <-- добавлено
                }));
                this.setState({
                    recommendations: loadedRecommendations,
                    currentPage: page,
                    totalEvents: data.total || 0,
                });
            })
            .catch((err) => console.error("Ошибка при загрузке Рекомендаций:", err));
    };

    // Обработка изменения поискового запроса
    handleSearchChange = (e) => {
        const newSearch = e.target.value;
        this.setState({search: newSearch});
    };
    // Обработка перехода на другую страницу
    handlePageClick = (pageNumber) => {
        const { activeTab, search, selectedTags } = this.state;
        if (activeTab === "allEvents") {
            this.loadEvents(pageNumber, search, selectedTags);
        } else {
            this.loadRecommendations(pageNumber, search, selectedTags);
        }
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
            const { activeTab, search } = prevState;
            if (activeTab === "allEvents") {
                this.loadEvents(1, search, selectedTags);
            } else {
                this.loadRecommendations(1, search, selectedTags);
            }
            return { selectedTags };
        });
    };

    // Переключение вкладок
    handleTabChange = (tab) => {
        this.setState({ activeTab: tab, currentPage: 1 }, () => {
            const { search, selectedTags } = this.state;
            if (tab === "allEvents") {
                this.loadEvents(1, search, selectedTags);
            } else {
                this.loadRecommendations(1, search, selectedTags);
            }
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

    render() {
        const {navigate} = this.props;
        const {events, recommendations, tags, search, currentPage, eventsPerPage, totalEvents, sidebarOpen, activeTab} = this.state;
        const totalPages = Math.ceil(totalEvents / eventsPerPage);
        const displayEvents = activeTab === "allEvents" ? events : recommendations;
        const groupedEvents = this.groupEventsByLocation(displayEvents);



        return (
            <div className="events-container">
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title=""
                    navigate={navigate}/>
                <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={this.context.user}/>

                <div className="content-panels">
                    <motion.div className="left-panel" initial={{ opacity: 0, x: -50 }} animate={{ opacity: 1, x: 0 }}
                                transition={{ duration: 0.5 }}>
                        {/* Вкладки */}
                        <div className="tabs-wrapper">
                            <button
                                className={`tab-button ${this.state.activeTab === "allEvents" ? "active" : ""}`}
                                onClick={() => this.handleTabChange("allEvents")}
                            >
                                Все мероприятия
                            </button>
                            <button
                                className={`tab-button ${this.state.activeTab === "recommendations" ? "active" : ""}`}
                                onClick={() => this.handleTabChange("recommendations")}
                            >
                                Рекомендации
                            </button>
                        </div>

                        {/* Поиск */}
                        <div className="search-wrapper">
                            <input
                                type="text"
                                placeholder="Поиск мероприятий"
                                className="search-input"
                                value={search}
                                onChange={this.handleSearchChange}
                                onKeyDown={(e) => {
                                    if (e.key === "Enter") {
                                        if (this.state.activeTab === "allEvents") {
                                            this.loadEvents(1, this.state.search);
                                        } else {
                                            this.loadRecommendations(1, this.state.search);
                                        }
                                    }
                                }}
                            />
                            <button
                                className="search-button-inside"
                                onClick={() => {
                                    if (this.state.activeTab === "allEvents") {
                                        this.loadEvents(1, this.state.search);
                                    } else {
                                        this.loadRecommendations(1, this.state.search);
                                    }
                                }}
                                aria-label="Поиск"
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none"
                                     viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M21 21l-4.35-4.35M11 19a8 8 0 100-16 8 8 0 000 16z"/>
                                </svg>
                            </button>
                        </div>
                        {/* Фильтр по тегам */}
                        <div className="tags-filter-wrapper">
                            {tags.map((tag) => {
                                const isSelected = this.state.selectedTags && this.state.selectedTags.includes(tag.name);
                                return (
                                    <button
                                        key={tag.name}
                                        onClick={() => this.toggleTag(tag.name)}
                                        className={`tag-button ${isSelected ? "selected" : ""}`}
                                    >
                                        {this.context.user && this.context.user.id && this.context.user.role === "MEMBER"  && (
                                            <FavoriteStar
                                                tag={tag}
                                                userId={this.context.user.id}
                                                isFavorite={tag.isFavorite || false}
                                                toggleFavorite={this.toggleFavorite}
                                            />
                                        )}
                                        {tag.name}
                                        {isSelected && <span className="remove-icon">×</span>}
                                    </button>
                                );
                            })}
                        </div>
                        {/* Верхняя пагинация */}
                        <Pagination totalPages={totalPages} currentPage={currentPage}
                                    handlePageClick={this.handlePageClick}/>

                        {/* Карточки событий */}
                        {displayEvents.map((event) => (
                            <motion.div key={event.id} className={`event-card ${event.tags.some((tag) => this.state.tags.some((element) => element.name === tag && element.isFavorite)) ? 'favorite' : ''}`}
                            >
                                <img
                                    src={event.imageUrl ? `data:image/jpeg;base64,${event.imageUrl}` : defaultEventImage}
                                    alt={event.title}
                                    className="event-image"
                                />
                                <div className="event-info">
                                    <div className="event-title-container">
                                        <div className="event-title">{event.title}</div>
                                        <div className="event-date">{event.date}</div>
                                    </div>
                                    <p className="event-short-description">{event.shortDescription}</p>
                                    <p className="event-location">{event.location}</p>
                                    <div className="event-tags">
                                        {event.tags.map((tag, idx) => (
                                            <span key={idx} className={`event-tag`}>{tag}</span>
                                        ))}
                                    </div>
                                    <div className="card-buttons">
                                        <div className="button-group">
                                            <button onClick={() => navigate(`/events/${event.id}`)}
                                                    className="event-button details">
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
                                        <div
                                            className={`event-format ${event.format.toLowerCase()}`}>{event.format === "ONLINE" ? "Онлайн" : "Офлайн"}</div>
                                    </div>
                                </div>

                            </motion.div>
                        ))}
                        {/* Нижняя пагинация */}
                        <Pagination totalPages={totalPages} currentPage={currentPage}
                                    handlePageClick={this.handlePageClick}/>
                    </motion.div>
                    {/* Карта */}
                    <motion.div className="right-panel" initial={{opacity: 0, x: 50}} animate={{opacity: 1, x: 0}}
                                transition={{duration: 0.5}}>
                        <MapContainer center={[55.75, 37.61]} zoom={11} minZoom={2} style={{height: "100%"}}
                                      maxBounds={[[-90, -180], [90, 180]]}>
                            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                       attribution="&copy; OpenStreetMap contributors" maxZoom={18}/>
                            {events.length > 0 && <FitToAllMarkers events={displayEvents}/>}
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
                                    const hasFavoriteTag = group[0].tags.some((tag) => this.state.tags.some((favtag) => favtag.name === tag && favtag.isFavorite));
                                    let icon = group[0].format === "ONLINE" ? onlineIcon : offlineIcon;
                                    if (hasFavoriteTag)
                                        icon = group[0].format === "ONLINE" ? onlineIconStar : offlineIconStar;
                                        //console.log(group[0].title)
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
                                                <MultiEventPopup events={group} navigate={navigate} favoriteTags={this.state.tags.filter((tag)=> tag.isFavorite)}
                                                                 initialEventId={initialEventId}/>
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

function MultiEventPopup({events, navigate, initialEventId, favoriteTags}) {
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
            {favoriteTags && event.tags.some((tag) => favoriteTags.find((favoriteTag) => favoriteTag.name === tag)) && (
                <p>Избранные теги: {event.tags.filter((tag) => favoriteTags.find((favoriteTag) => favoriteTag.name === tag)).join(", ")}</p>
            )}
            <button onClick={() => navigate(`/events/${event.id}`)} className="event-button details">
                Подробнее
            </button>
            {total > 1 && (
                <div className="popup-pagination">
                    <button className="popup-page-button"
                            onClick={() => setPage((p) => (p === 0 ? total - 1 : p - 1))}>{"<"}</button>
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
