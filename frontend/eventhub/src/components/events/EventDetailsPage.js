import React, { useEffect, useState, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../../css/EventDetailsPage.css";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";

const formatDateRange = (start, end) => {
    const options = { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" };
    const startStr = start.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    const endStr = end.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    return `${startStr} - ${endStr}`;
};

function checkSubscription(id, user, setIsSubscribed) {
    fetch(`http://localhost:9500/api/v1/events/${id}/members/${user.id}`, {
        method: "GET",
        credentials: "include",
    })
        .then((res) => res.json())
        .then((data) => {
            console.log(data);
            if (data.eventId == id && data.userId == user.id)
                setIsSubscribed(true);
        })
}

const EventDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useContext(UserContext);
    const [event, setEvent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isSubscribed, setIsSubscribed] = useState(false);

    useEffect(() => {
        fetch(`http://localhost:9500/api/v1/events/${id}`, {
            method: "GET",
            credentials: "include",
        })
            .then((res) => res.json())
            .then((data) => {
                setEvent(data);
                setLoading(false);
                // Проверим, подписан ли пользователь на мероприятие
                checkSubscription(id, user, setIsSubscribed);
            })
            .catch((err) => {
                console.error("Ошибка загрузки мероприятия:", err);
                setLoading(false);
            });
    }, [id, user.id]);

    const handleSubscription = () => {
        const method = isSubscribed ? "DELETE" : "POST";
        fetch(`http://localhost:9500/api/v1/members/${user.id}/subscribe/${id}`, {
            method: method,
            credentials: "include",
        })
            .then(() => {
                setIsSubscribed(!isSubscribed);
            })
            .catch((err) => {
                console.error("Ошибка при изменении подписки:", err);
            });
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
                            <ul>
                                {event.files.map((file, idx) => (
                                    <li key={idx}>
                                        <a href={file.url} target="_blank" rel="noopener noreferrer">{file.name}</a>
                                    </li>
                                ))}
                            </ul>
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
