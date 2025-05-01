import React, { useEffect, useState, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../../css/EventDetailsPage.css";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";
import API_BASE_URL from "../../config";

const formatDateRange = (start, end) => {
    const options = { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" };
    const startStr = start.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    const endStr = end.toLocaleString("ru-RU", options).replace(",", "").replaceAll("/", ".");
    return `${startStr} - ${endStr}`;
};

async function checkSubscription(id, user) {
    const res = await fetch(`${API_BASE_URL}/v1/events/${id}/members/${user.id}`, {
        method: "GET",
        credentials: "include",
    });
    if (!res.ok) return false;

    const data = await res.json();
    return data.eventId === parseInt(id) && data.userId === user.id;
}

const checkAuth = (setUser) => {
    fetch(`${API_BASE_URL}/auth/me`, {
        method: 'GET',
        credentials: 'include',
    })
        .then((res) => {
            if (!res.ok) throw new Error("Не авторизован");
            return res.json();
        })
        .then((data) => {
            setUser({
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

const EventDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user, setUser } = useContext(UserContext);
    const [event, setEvent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isSubscribed, setIsSubscribed] = useState(false);

    useEffect(() => {
        checkAuth(setUser);
        const fetchEvent = async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/v1/events/${id}`, {
                    method: "GET",
                    credentials: "include",
                });

                const data = await res.json();
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

    const handleSubscription = () => {
        if (!user || !user.id) {
            navigate("/login", { state: { from: `/events/${id}` } });
        }
        const method = isSubscribed ? "DELETE" : "POST";
        fetch(`${API_BASE_URL}/v1/members/${user.id}/subscribe/${id}`, {
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
