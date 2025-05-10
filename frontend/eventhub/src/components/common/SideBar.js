import React from "react";
import {useNavigate} from "react-router-dom";
import "../../css/SideBar.css";

const SideBar = ({sidebarOpen, sidebarRef, user}) => {
    const navigate = useNavigate();
    return (
        <div className={`profile-sidebar ${sidebarOpen ? 'open' : 'closed'}`} ref={sidebarRef}>
            <ul>
                <li onClick={() => {
                    navigate("/profile");
                    sidebarOpen = false;
                }}>
                    <i className="bi bi-person-fill"></i> Профиль
                </li>

                <li onClick={() => navigate("/events")}>
                    <i className="bi bi-calendar-event-fill"></i> Мероприятия
                </li>

                {user.role === 'MEMBER' && (
                    <>
                        <li onClick={() => navigate("/friends")}>
                            <i className="bi bi-people-fill"></i> Мои друзья
                        </li>

                        <li onClick={() => navigate("/my-events")}>
                            <i className="bi bi-calendar-check-fill"></i> Мои мероприятия
                        </li>

                        <li onClick={() => navigate("/favorites")}>
                            <i className="bi bi-star-fill"></i> Избранное
                        </li>
                    </>
                )}
                {user.role === 'ORGANIZER' && (
                    <li onClick={() => navigate("/my-events")}>
                        <i className="bi bi-calendar-check-fill"></i> Мои мероприятия
                    </li>
                )}
                {user.role === 'MODERATOR' && (
                    <>
                        <li onClick={() => navigate("/accreditation")}>
                            <i className="bi bi-clipboard-check-fill"></i> Аккредитация организаций
                        </li>

                        <li onClick={() => navigate("/moderator-management")}>
                            <i className="bi bi-shield-lock-fill"></i> Управление модераторами
                        </li>
                    </>
                )}
                <li onClick={() => navigate("/logout")}>
                    <svg xmlns="http://www.w3.org/2000/svg" height="16" width="16" viewBox="0 0 512 512">
                        <path
                            d="M377.9 105.9L500.7 228.7c7.2 7.2 11.3 17.1 11.3 27.3s-4.1 20.1-11.3 27.3L377.9 406.1c-6.4 6.4-15 9.9-24 9.9c-18.7 0-33.9-15.2-33.9-33.9l0-62.1-128 0c-17.7 0-32-14.3-32-32l0-64c0-17.7 14.3-32 32-32l128 0 0-62.1c0-18.7 15.2-33.9 33.9-33.9c9 0 17.6 3.6 24 9.9zM160 96L96 96c-17.7 0-32 14.3-32 32l0 256c0 17.7 14.3 32 32 32l64 0c17.7 0 32 14.3 32 32s-14.3 32-32 32l-64 0c-53 0-96-43-96-96L0 128C0 75 43 32 96 32l64 0c17.7 0 32 14.3 32 32s-14.3 32-32 32z"/>
                    </svg>
                    <text> Выход</text>
                </li>
            </ul>
        </div>
    );
};

export default SideBar