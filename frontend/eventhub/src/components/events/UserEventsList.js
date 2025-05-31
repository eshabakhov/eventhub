import {useNavigate, useParams} from "react-router-dom";
import React, {Component, useState} from "react";
import UserContext from "../../UserContext";
import CrossIcon from "../../img/x.png";
import {motion} from "framer-motion";
import {MapContainer, Marker, Popup, TileLayer, useMap} from "react-leaflet";
import MarkerClusterGroup from 'react-leaflet-cluster';
import "../../css/MyEventsList.css";
import leaflet from "leaflet";
import onlineIconImg from "../../img/online-marker.png";
import offlineIconImg from "../../img/offline-marker.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import API_BASE_URL from "../../config";
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import Pagination from "../common/Pagination";
import defaultEventImage from "../../img/image-512.png";
import api from "../common/AxiosInstance";
import ConfirmModal from "../common/ConfirmModal";

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
}

function withParams(Component) {
    return props => <Component {...props} params={useParams()}/>;
}

// Форматирование даты
const formatDateRange = (start, end) => {
    const options = {day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit"};
    const startStr = start.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    const endStr = end.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    return `${startStr} - ${endStr}`;
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

class UserEventsList extends Component {
    static contextType = UserContext;

    constructor(props) {
        super(props);
        this.markerRefs = React.createRef();
        this.markerRefs.current = {};
        this.state = {
            events: [],
            eventsOpen: false,
            username: "",
            search: "",
            focusedEvent: null,
            currentPage: 1,
            eventsPerPage: 10,
            totalEvents: 0,
            tags: [],
            selectedTags: [],
            showConfirmModal: false,
            selectedEvent: null,
            sidebarOpen: false,
            isOrganizersPath: false,
            isMembersPath: false
        };
    }

    sidebarRef = React.createRef();

    componentDidMount() {
        const currentUrl = window.location.href;
        const isOrganizersPath = currentUrl.includes('/organizers/');
        const isMembersPath = currentUrl.includes('/users/');
        this.setState({isOrganizersPath, isMembersPath});
        if (isMembersPath) {
            const memberId = this.props.params;
            api.get(`${API_BASE_URL}/v1/users/${memberId.id}`, {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then((response) => {
                    const data = response.data;
                    this.setState({username: "Мероприятия " + data.username});
                })
                .catch((error) => {
                    console.error('Ошибка при загрузке профиля:', error);
                });
            api.get(`/v1/friends/${memberId.id}/isfriend`, {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then((response) => {
                    const data = response.data;
                    if (data.privacy === 'PUBLIC' || data.privacy === 'ONLY_FRIENDS' && data.friendly === true) {
                        this.setState({eventsOpen: true});
                        this.loadEvents(1, this.state.search);
                        this.loadTags();
                        document.addEventListener("mousedown", this.handleClickOutside);
                    } else {
                        this.setState({eventsOpen: false});
                    }
                })
                .catch((error) => {
                    console.error('Ошибка при загрузке профиля:', error);
                });
        } else {
            const organizerId = this.props.params;
            api.get(`${API_BASE_URL}/v1/users/organizers/${organizerId.id}`, {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then((response) => {
                    const data = response.data;
                    this.setState({username: "Мероприятия " + data.name});
                    this.setState({eventsOpen: true});
                    this.loadEvents(1, this.state.search);
                    this.loadTags();
                    document.addEventListener("mousedown", this.handleClickOutside);
                })
                .catch((error) => {
                    console.error('Ошибка при загрузке профиля:', error);
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
            })
            .catch((err) => console.error("Ошибка при загрузке тегов:", err));
    };

    // Загрузка мероприятий
    loadEvents = (page, search = "", searchTags = []) => {
        const {eventsPerPage} = this.state;
        const searchParam = search ? `&search=${encodeURIComponent(search)}` : "";
        const searchTagsParam = searchTags.length > 0 ? `&tags=${searchTags.join(",")}` : "";

        let url = '';
        if (this.state.isMembersPath) {
            const memberId = this.props.params
            url = `${API_BASE_URL}/v1/members/${memberId.id}/events?page=${page}&pageSize=${eventsPerPage}${searchParam}${searchTagsParam}`
        } else {
            const organizerId = this.props.params
            url = `${API_BASE_URL}/v1/events/organizers/${organizerId.id}?page=${page}&pageSize=${eventsPerPage}${searchParam}${searchTagsParam}`
        }
        fetch(url, {
            method: "GET",
            headers: {"Content-Type": "application/json"},
            credentials: "include",
        })
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

    // Открытие модального окна для отказа от участия
    handleDeclineClick = (event) => {
        this.setState({
            showConfirmModal: true,
            selectedEvent: event
        });
    };
    // Открытие модального окна удаления мероприятия
    handleDeleteClick = (event) => {
        this.setState({
            showConfirmModal: true,
            selectedEvent: event
        });
    }

    // Переход к редактированию мероприятия
    handleEditClick = (event) => {
        this.props.navigate(`/event-edit/${event.id}`);
    }

    // Открытие модального окна для отказа от участия
    handleDeclineClick = (event) => {
        this.setState({
            showConfirmModal: true,
            selectedEvent: event
        });
    };

    // Закрытие модального окна
    handleCloseModal = () => {
        this.setState({
            showConfirmModal: false,
            selectedEvent: null
        });
    };

    // Подтверждение
    handleConfirmDecline = () => {
        const {selectedEvent} = this.state;
        const {user} = this.context;
        if (!selectedEvent || !user) {
            this.handleCloseModal();
            return;
        }
        if (user.role === "MEMBER") {
            this.refuceToParticipation(selectedEvent, user);
        }
        if (user.role === "ORGANIZER") {
            this.deleteEvent(selectedEvent);
        }
    };
    // Отмена участия
    refuceToParticipation = (selectedEvent, user) => {
        fetch(`${API_BASE_URL}/v1/members/${user.id}/subscribe/${selectedEvent.id}`, {
            method: "DELETE",
            headers: {"Content-Type": "application/json"},
            credentials: "include",
        })
            .then((res) => {
                if (res.ok) {
                    // Обновляем список мероприятий после успешного отказа
                    this.loadEvents(this.state.currentPage, this.state.search, this.state.selectedTags);
                } else {
                    console.error("Ошибка при отказе от участия");
                }
            })
            .catch((err) => console.error("Ошибка при отказе от участия:", err))
            .finally(() => {
                this.handleCloseModal();
            });
    }

    // Удаление мероприятия
    deleteEvent = (selectedEvent) => {
        fetch(`${API_BASE_URL}/v1/events/${selectedEvent.id}`, {
            method: "DELETE",
            headers: {"Content-Type": "application/json"},
            credentials: "include",
        })
            .then((res) => {
                if (res.ok) {
                    // Обновляем список мероприятий после успешного удаления
                    this.loadEvents(this.state.currentPage, this.state.search, this.state.selectedTags);
                } else {
                    console.error("Ошибка при удалении мероприятия");
                }
            })
            .catch((err) => console.error("Ошибка при удалении мероприятия:", err))
            .finally(() => {
                this.handleCloseModal();
            });
    }

    // Обработка изменения поискового запроса
    handleSearchChange = (e) => {
        const newSearch = e.target.value;
        this.setState({search: newSearch});
    };

    // Обработка перехода на другую страницу
    handlePageClick = (pageNumber) => {
        this.loadEvents(pageNumber, this.state.search, this.state.selectedTags);
        const el = document.getElementsByClassName("left-panel")[0];
        el.scrollTo(0, 0);
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
        const {
            events,
            username,
            tags,
            search,
            currentPage,
            eventsPerPage,
            totalEvents,
            showConfirmModal,
            selectedEvent,
            sidebarOpen
        } = this.state;
        const totalPages = Math.ceil(totalEvents / eventsPerPage);
        const groupedEvents = this.groupEventsByLocation(events);
        const memberId = this.props.params

        return (
            <div className="events-container">
                {/* Модальное окно подтверждения */}
                <ConfirmModal
                    isOpen={showConfirmModal}
                    headerText={
                        this.context.user && selectedEvent
                            ? this.context.user.role === "MEMBER"
                                ? "Подтверждение"
                                : "Внимание!"
                            : ""
                    }
                    mainText={
                        this.context.user && selectedEvent
                            ? this.context.user.role === "MEMBER"
                                ? `Вы уверены, что хотите отказаться от участия в мероприятии "${selectedEvent.title}"?`
                                : `Вы уверены, что хотите навсегда удалить мероприятие "${selectedEvent.title}"? Действие невозможно будет отменить!`
                            : ""
                    }
                    cancelText="Отмена"
                    confirmText={
                        this.context.user && selectedEvent
                            ? this.context.user.role === "MEMBER"
                                ? "Подтвердить"
                                : "Удалить"
                            : ""
                    }
                    onClose={this.handleCloseModal}
                    onConfirm={this.handleConfirmDecline}
                />
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title={username}
                    user={this.context.user}
                    navigate={navigate}/>

                <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={this.context.user}/>

                <div className="content-panels">
                    <motion.div className="left-panel" initial={{opacity: 0, x: -50}} animate={{opacity: 1, x: 0}}
                                transition={{duration: 0.5}}>
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
                            <button className="search-button-inside"
                                    onClick={() => this.loadEvents(1, this.state.search)} aria-label="Поиск">
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none"
                                     viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M21 21l-4.35-4.35M11 19a8 8 0 100-16 8 8 0 000 16z"/>
                                </svg>
                            </button>
                        </div>
                        {/* Верхняя пагинация */}
                        <Pagination totalPages={totalPages} currentPage={currentPage}
                                    handlePageClick={this.handlePageClick}/>

                        {/* Карточки событий */}
                        {events.map((event) => (
                            <motion.div key={event.id} className="event-card" whileHover={{scale: 1.02}}>
                                <img
                                    src={event.imageUrl ? `data:image/jpeg;base64,${event.imageUrl}` : defaultEventImage}
                                    alt={event.title}
                                    className="event-image"
                                />
                                <div className="event-info">
                                    <div className="event-title-container">
                                        <div className="event-title">{event.title}</div>
                                        <div>
                                            {this.context.user && this.context.user.id == memberId.id && this.context.user.role === "MEMBER" && (
                                                <div className="params-buttons">
                                                    <button
                                                        className="delete-button"
                                                        title='Отказаться от участия'
                                                        onClick={() => this.handleDeclineClick(event)}
                                                    >
                                                        <img src={CrossIcon} alt='Удалить' className="icon"/>
                                                    </button>
                                                </div>
                                            )}
                                            <div className="event-date-down">{event.date}</div>
                                        </div>
                                    </div>
                                    <p className="event-short-description">{event.shortDescription}</p>
                                    <p className="event-location">{event.location}</p>
                                    <div className="event-tags">
                                        {event.tags.map((tag, idx) => (
                                            <span key={idx} className="event-tag">{tag}</span>
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
                    {/*Карта*/}
                    <motion.div className="right-panel" initial={{opacity: 0, x: 50}} animate={{opacity: 1, x: 0}}
                                transition={{duration: 0.5}}>
                        <MapContainer center={[55.75, 37.61]} zoom={11} minZoom={2} style={{height: "100%"}}
                                      maxBounds={[[-90, -180], [90, 180]]}>
                            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                       attribution="&copy; OpenStreetMap contributors" maxZoom={18}/>
                            {events.length > 0 && <FitToAllMarkers events={events}/>}
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
                                    const icon = group[0].format === "ONLINE" ? onlineIcon : offlineIcon
                                    const initialEventId = this.state.focusedEvent?.id;
                                    return (
                                        <Marker key={key} position={[lat, lng]} icon={icon}
                                                ref={(ref) => (this.markerRefs.current[key] = ref)}>
                                            <Popup>
                                                <MultiEventPopup events={group} navigate={navigate}
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

{/* PopUp для сгрупированных событий */
}

function MultiEventPopup({events, navigate, initialEventId}) {
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

export default withNavigation(withParams(UserEventsList));