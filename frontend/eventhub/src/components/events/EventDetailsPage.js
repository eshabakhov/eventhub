import React, {useEffect, useState, useContext} from "react";
import {useParams, useNavigate} from "react-router-dom";
import "../../css/EventDetailsPage.css";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";
import api from "../common/AxiosInstance";
import CurrentUser from "../common/CurrentUser";
import {MapContainer, TileLayer, Marker, Popup, useMap} from "react-leaflet";
import leaflet from "leaflet";
import "leaflet/dist/leaflet.css";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import offlineIconImg from "../../img/offline-marker.png";
import onlineIconImg from "../../img/online-marker.png";
import Header from "../common/Header";
import defaultEventImage from "../../img/image-512.png";

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

const formatDateRange = (start, end) => {
    const options = {day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit"};
    const startStr = start.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    const endStr = end.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    return `${startStr} - ${endStr}`;
};

function CenterMap({center, zoom}) {
    const map = useMap();
    useEffect(() => {
        map.setView(center, zoom);
    }, [center, zoom]);
    return null;
}

async function checkSubscription(id, user) {
    const res = await api.get(`/v1/members/${user.id}/subscribe/${id}`, {
        credentials: "include",
    });
    if (res.status !== 200) return false;

    const data = await res.data;
    return data.eventId === parseInt(id) && data.userId === user.id;
}

const getFileIcon = (fileName) => {
    const extension = fileName.split('.').pop().toLowerCase();
    switch (extension) {
        case 'pdf':
            return 'bi-file-earmark-pdf';
        case 'doc':
        case 'docx':
            return 'bi-file-earmark-word';
        case 'xls':
        case 'xlsx':
            return 'bi-file-earmark-excel';
        case 'ppt':
        case 'pptx':
            return 'bi-file-earmark-ppt';
        case 'jpg':
        case 'jpeg':
        case 'png':
        case 'gif':
            return 'bi-file-earmark-image';
        case 'zip':
        case 'rar':
            return 'bi-file-earmark-zip';
        default:
            return 'bi-file-earmark';
    }
};

const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
};

const EventDetailsPage = () => {
    const {id} = useParams();
    const navigate = useNavigate();
    const {user, setUser} = useContext(UserContext);
    const [event, setEvent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isSubscribed, setIsSubscribed] = useState(false);
    const [isOwner, setIsOwner] = useState(false);

    useEffect(() => {
        const fetchEvent = async () => {
            try {
                const res = await api.get(`/v1/events/${id}`, {
                    credentials: "include",
                });

                const data = res.data;
                setEvent(data);
                setLoading(false);

                if (user && user.id && user.role === "MEMBER") {
                    const subscribed = await checkSubscription(id, user);
                    setIsSubscribed(subscribed);
                } else {
                    setIsSubscribed(false);
                }
                if (user && user.id && user.role === "ORGANIZER") {
                    const isOwner = data.organizerId === user.id;
                    setIsOwner(isOwner);
                }
            } catch (err) {
                console.error("Ошибка загрузки мероприятия:", err);
                setLoading(false);
            }
        };
        CurrentUser.fetchCurrentUser(setUser);
        fetchEvent();
    }, [id, user?.id]);

    const handleSubscription = async () => {
        if (!user || !user.id) {
            navigate("/login", {state: {from: `/events/${id}`}});
            return;
        }
        if (isSubscribed) {
            await api.delete(`/v1/members/${user.id}/subscribe/${id}`, {
                credentials: "include",
            })
                .then(() => {
                    setIsSubscribed(!isSubscribed);
                })
                .catch((err) => {
                    console.error("Ошибка при изменении подписки:", err);
                });
        } else {
            api.post(`/v1/members/${user.id}/subscribe/${id}`, {
                credentials: "include",
            })
                .then(() => {
                    setIsSubscribed(!isSubscribed);
                })
                .catch((err) => {
                    console.error("Ошибка при изменении подписки:", err);
                });
        }
    };

    const handleFileDownload = async (fileId, fileName) => {
        try {
            const response = await api.get(`/v1/events/file/download/${fileId}`, {
                credentials: 'include',
                responseType: 'blob'
            });
            const blob = response.data;
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(downloadUrl);
        } catch (error) {
            console.error('Ошибка при скачивании файла:', error);
            alert('Произошла ошибка при скачивании файла');
        }
    };

    if (loading) return <div className="event-details-container">Загрузка...</div>;
    if (!event) return <div className="event-details-container">Мероприятие не найдено</div>;

    // Координаты для карты
    const hasCoordinates = event.latitude && event.longitude;
    const position = hasCoordinates ? [event.latitude, event.longitude] : [55.75, 37.61];
    const zoom = event.format === "ONLINE" ? 4 : 15;

    return (
        <div className="event-details-container">

            <Header
                navigate={navigate}
                burgerVisible={false}
            />

            <div className="event-details-wrapper">
                <div className="event-details-content">
                    <div className="event-title-header">
                        <img
                            src={event.pictures ? `data:image/jpeg;base64,${event.pictures}` : defaultEventImage}
                            alt={event.title}
                            className="event-details-image"
                        />
                        <h1 className="event-details-title">{event.title}</h1>
                    </div>

                    {event.tags?.length > 0 && (
                        <div className="event-tags">
                            {event.tags.map((tag, idx) => (
                                <span key={idx} className="event-tag">{tag.name}</span>
                            ))}
                        </div>
                    )}
                    <div className="event-description-header">Формат:
                        <div className="event-details-format">
                            {event.format === "ONLINE" ? " Онлайн" : " Офлайн"}
                        </div>
                    </div>

                    <div className="event-description-header"> Дата проведения:
                        <div className="event-details-date">
                            {formatDateRange(event.startDateTime, event.endDateTime)}
                        </div>
                    </div>

                    <div className="event-description-header"> Адрес:
                        <div className="event-location">
                            {event.location}
                        </div>
                    </div>

                    <h2 className="event-description-header">О событии:</h2>
                    <p className="event-description">{event.description}</p>

                    {/* Карта */}
                    {event.format === "OFFLINE" && hasCoordinates && (
                        <div className="event-map-section">
                            <h3 className="map-title">Место проведения</h3>
                            <div className="map-container">
                                <MapContainer
                                    center={position}
                                    zoom={event.format === "OFFLINE" ? 18 : 10}
                                    style={{height: "300px", borderRadius: "8px"}}
                                >
                                    <TileLayer
                                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                                    />
                                    <CenterMap center={position} zoom={event.format === "OFFLINE" ? 18 : 10}/>
                                    <Marker
                                        position={position}
                                        icon={event.format === "ONLINE" ? onlineIcon : offlineIcon}
                                    >
                                        <Popup>{event.location}</Popup>
                                    </Marker>
                                </MapContainer>
                            </div>
                        </div>
                    )}

                    {event.files?.length > 0 && (
                        <div className="event-files-section">
                            <h3 className="files-title">Прикрепленные файлы</h3>
                            <div className="files-container">
                                {event.files.map((file, idx) => (
                                    <div
                                        key={idx}
                                        className="file-item"
                                        onClick={() => handleFileDownload(file.fileId, file.fileName)}
                                    >
                                        <div className="file-icon-name">
                                            <i className={`bi ${getFileIcon(file.fileName)}`}></i>
                                            <div className="file-info">
                                                <span className="file-name-type">
                                                    <span className="file-name" title={file.fileName}>
                                                        {file.fileName.split('.')[0]}
                                                    </span>
                                                    <span className="file-type" title={file.fileName}>
                                                        .{file.fileName.split('.').pop()}
                                                    </span>
                                                </span>

                                                <span className="file-size">
                                                    {file.fileSize ? formatFileSize(file.fileSize) : 'N/A'}
                                                </span>
                                            </div>
                                        </div>
                                        <div className="download-icon">
                                            <i className="bi bi-download"></i>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="buttons-container">
                        <button className="back-button-event-details" onClick={() => navigate("/events")}>
                            Назад к списку
                        </button>
                        {user?.role === "MEMBER" && (
                            <button
                                className={`subscription-button ${isSubscribed ? "non-subscribed" : ""}`}
                                onClick={handleSubscription}
                            >
                                {isSubscribed ? "Отказаться от участия" : "Принять участие"}
                            </button>
                        )}
                        {user?.role === "ORGANIZER" && isOwner && (
                            <button
                                className={`subscription-button`}
                                onClick={() => navigate(`/event-edit/${event.id}`)}
                            >
                                Редактировать
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default EventDetailsPage;