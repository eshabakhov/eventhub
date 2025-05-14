import React, { useEffect, useState, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../../css/EventDetailsPage.css";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";
import api from "../common/AxiosInstance";

const formatDateRange = (start, end) => {
    const options = { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" };
    const startStr = start.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    const endStr = end.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    return `${startStr} - ${endStr}`;
};

async function checkSubscription(id, user) {
    const res = await api.get(`/v1/members/${user.id}/subscribe/${id}`, {
        credentials: "include",
    });
    if (!res.status !== 200) return false;

    const data = await res.data;
    return data.eventId === parseInt(id) && data.userId === user.id;
}

const EventDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user, setUser } = useContext(UserContext);
    const [event, setEvent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isSubscribed, setIsSubscribed] = useState(false);

    useEffect(() => {
        const fetchEvent = async () => {
            try {
                const res = await api.get(`/v1/events/${id}`, {
                    credentials: "include",
                });

                const data = res.data;
                setEvent(data);
                setLoading(false);

                if (user && user.id) {
                    const subscribed = await checkSubscription(id, user);
                    setIsSubscribed(subscribed);
                } else {
                    setIsSubscribed(false);
                }
            } catch (err) {
                console.error("Ошибка загрузки мероприятия:", err);
                setLoading(false);
            }
        };
        fetchEvent();
    }, [id, user?.id]);

    const handleSubscription = async () => {
        if (!user || !user.id) {
            navigate("/login", { state: { from: `/events/${id}` } });
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
            const response = await api.get(`/v1/events/file/download1/${fileId}`, {
                credentials: 'include',
                responseType: 'blob'
            });
            const blob = response.data;
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.setAttribute('download', fileName); // Указываем имя файла
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(downloadUrl); // Освобождаем память
        } catch (error) {
            console.error('Ошибка при скачивании файла:', error);
            alert('Произошла ошибка при скачивании файла');
        }
    };

    if (loading) return <div className="styles.event-details-container">Загрузка...</div>;
    if (!event) return <div className="styles.event-details-container">Мероприятие не найдено</div>;

    return (
        <div className="event-details-container">
            <div className="header-bar">
                <div className="top-logo" onClick={() => navigate("/events")} style={{ cursor: "pointer" }}>
                    <img src={EventHubLogo} alt="Logo" className="logo" />
                </div>
                <div className="login-button-container">
                    <ProfileDropdown navigate={navigate} />
                </div>
            </div>

            <div className="event-details-wrapper">
                <div className="event-details-content">
                    <h1>{event.title}</h1>
                    <p className="event-format">{event.format === "ONLINE" ? "Онлайн" : "Офлайн"}</p>
                    <p className="event-details-date">{formatDateRange(event.startDateTime, event.endDateTime)}</p>
                    <p className="event-location"><strong>Место проведения:</strong> {event.location}</p>
                    <p className="event-description">{event.description}</p>

                    {event.tags?.length > 0 && (
                        <div className="event-tags">
                            <strong>Теги:</strong>{" "}
                            {event.tags.map((tag, idx) => (
                                <span key={idx} className="event-tag">{tag.name}</span>
                            ))}
                        </div>
                    )}

                    {event.files?.length > 0 && (
                        <div className="event-files">
                            <strong>Файлы:</strong>
                            <div className="file-tags-container">
                                {event.files.map((file, idx) => (
                                    <div
                                        key={idx}
                                        className="file-tag"
                                        onClick={(e) => {
                                            e.preventDefault();
                                            handleFileDownload(file.fileId, file.fileName);
                                        }}
                                    >
                                        {file.fileName}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Контейнер для кнопок */}
                    <div className="buttons-container">
                        <button className="back-button-event-details" onClick={() => navigate("/events")}>Назад к списку</button>

                        {/* Кнопка для подписки */}
                        <button className={`subscription-button ${isSubscribed ? "non-subscribed" : ""}`} onClick={handleSubscription}>
                            {isSubscribed ? "Отказаться от участия" : "Принять участие"}
                        </button>
                    </div>
                    
                </div>
            </div>
        </div>
    );
};

export default EventDetailsPage;
