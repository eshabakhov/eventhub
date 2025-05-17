import ProfileDropdown from "../profile/ProfileDropdown";
import React from "react";
import {useNavigate} from "react-router-dom";
import Logo from "./Logo";
import "../../css/Header.css";

const Header = ({onBurgerButtonClick, title, user, burgerVisible = true}) => {
    const navigate = useNavigate();
    return (
        <div className="header-bar">
            <div className="header-left">

                <div className={`burger-button ${!burgerVisible ? 'burger-hidden' : ''} `} onClick={onBurgerButtonClick}>
                    <i className="bi bi-list"/>
                </div>
                <Logo/>
            </div>
            <h1 className="header-title">{title}</h1>
            <div className="login-button-container">
                {window.location.pathname === "/my-events" && user && user.role === "ORGANIZER" && user.organizerAccredited && (
                    <button className="create-button" onClick={() => navigate("/create-event")}>
                        <span> + </span>
                        Создать мероприятие
                    </button>
                )}
                <ProfileDropdown navigate={navigate}/>
            </div>
        </div>
    );
}

export default Header;