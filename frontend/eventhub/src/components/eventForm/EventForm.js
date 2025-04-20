import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {withNavigation} from "../events/EventsPage";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import "../../css/EventForm.css";
import axios from "axios";


const formatDateForBackend = (dateTimeString) => {
    if (!dateTimeString) return '';

    const date = new Date(dateTimeString);

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${day}/${month}/${year} ${hours}:${minutes}`;
};


class EventForm extends React.Component {
    static contextType = UserContext;

    constructor(props) {
        super(props);
        this.state = {
            title: '',
            description: '',
            shortDescription: '',
            location: '',
            startDateTime: '',
            endDateTime: '',
            format: 'OFFLINE',
            organizerId: '',
            errors: {}
        };
    }


    handleChange = (e) => {
        this.setState({
            [e.target.name]: e.target.value,
            errors: {
                ...this.state.errors,
                [e.target.name]: null
            }
        });
    };

    validate = () => {
        const { description, location, startDateTime, endDateTime } = this.state;
        const errors = {};

        if (!description) errors.description = 'Описание обязательно';
        if (!location) errors.location = 'Локация обязательна';
        if (!startDateTime) errors.startDateTime = 'Дата начала обязательна';
        if (!endDateTime) errors.endDateTime = 'Дата окончания обязательна';

        if (startDateTime && endDateTime && new Date(startDateTime) > new Date(endDateTime)) {
            errors.endDateTime = 'Дата окончания должна быть после даты начала';
        }

        this.setState({ errors });
        return Object.keys(errors).length === 0;
    };

    handleSubmit = (e) => {
        e.preventDefault();
        const { user } = this.context;

        if (this.validate()) {
            const eventData = {
                title: this.state.title,
                description: this.state.description,
                shortDescription: this.state.shortDescription,
                location: this.state.location,
                startDateTime: formatDateForBackend(this.state.startDateTime),
                endDateTime: formatDateForBackend(this.state.endDateTime),
                format: this.state.format,
                organizerId: user.id

            };
            console.log(user)

            console.log('Отправка данных мероприятия:', eventData);
            // Здесь можно добавить вызов API для сохранения мероприятия
            // this.props.onSubmit(eventData);

            fetch('http://localhost:9500/api/v1/events', {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(eventData)
            })
                .then(response => {
                    if (!response.ok) throw new Error('Network response was not ok');
                    //navigate('/');
                    this.props.navigate('/events');
                })
                .catch(error => {
                    console.error('Error saving event:', error);
                });


        }
    };

    render() {
        const { errors } = this.state;
        const { navigate } = this.props;

        return (
            <div className="events-f">
                {/* Верхняя панель */}
                <div className="header-bar">
                    <div className="top-logo" onClick={() => navigate("/events")} style={{ cursor: "pointer" }}>
                        <img src={EventHubLogo} alt="Logo" className="logo" />
                    </div>
                    <div className="return-button-container">
                        <button onClick={() => navigate("/events")} className="return-button">
                            Просмотр мероприятий
                        </button>
                    </div>
                    <div className="login-button-container">
                        {this.context.user && this.context.user.id ? (
                            <div className="profile-dropdown-container" ref={(ref) => (this.dropdownRef = ref)}>
                                <button onClick={this.toggleDropdown} className="login-button">
                                    Профиль
                                </button>
                                {this.state.showDropdown && (
                                    <div className="dropdown-menu">
                                        <button onClick={() => this.handleMenuClick("/profile")} className="dropdown-item">Профиль</button>
                                        <button onClick={() => this.handleMenuClick("/friends")} className="dropdown-item">Друзья</button>
                                        <button onClick={() => this.handleMenuClick("/logout")} className="dropdown-item">Выйти</button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="profile-dropdown-container" ref={(ref) => (this.dropdownRef = ref)}>
                                <button onClick={() => navigate("/login")} className="login-button">
                                    Войти
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                <div className="event-form">
                    <h2>Создание нового мероприятия</h2>
                    <form onSubmit={this.handleSubmit}>
                        <div className="form-group">
                            <label className="event-label">
                                Название мероприятия:
                                <input
                                    className="event-input"
                                    type="text"
                                    name="title"
                                    value={this.state.title}
                                    onChange={this.handleChange}
                                />
                                {errors.title && <span className="error-message">{errors.title}</span>}
                            </label>
                        </div>

                        <div className="form-group">
                            <label className="event-label">
                                Описание:
                                <input
                                    className="event-input"
                                    type="text"
                                    name="description"
                                    value={this.state.description}
                                    onChange={this.handleChange}
                                />
                                {errors.description && <span className="error-message">{errors.description}</span>}
                            </label>
                        </div>

                        <div className="form-group">
                            <label className="event-label">
                                Краткое описание:
                                <input
                                    className="event-input"
                                    type="text"
                                    name="shortDescription"
                                    value={this.state.shortDescription}
                                    onChange={this.handleChange}
                                />
                                {errors.shortDescription && <span className="error-message">{errors.shortDescription}</span>}
                            </label>
                        </div>

                        <div className="form-group">
                            <label className="event-label">
                                Локация:
                                <input
                                    className="event-input"
                                    type="text"
                                    name="location"
                                    value={this.state.location}
                                    onChange={this.handleChange}
                                />
                                {errors.location && <span className="error-message">{errors.location}</span>}
                            </label>
                        </div>

                        <div className="form-group">
                            <label className="event-label">
                                Дата начала:
                                <input
                                    className="event-time-input"
                                    type="datetime-local"
                                    name="startDateTime"
                                    value={this.state.startDateTime}
                                    onChange={this.handleChange}
                                />
                                {errors.startDateTime && <span className="error-message">{errors.startDate}</span>}
                            </label>
                        </div>

                        <div className="form-group">
                            <label className="event-label">
                                Дата окончания:
                                <input
                                    className="event-time-input"
                                    type="datetime-local"
                                    name="endDateTime"
                                    value={this.state.endDateTime}
                                    onChange={this.handleChange}
                                />
                                {errors.endDateTime && <span className="error-message">{errors.endDateTime}</span>}
                            </label>
                        </div>

                        <div className="form-group">
                            <label className="event-label">
                                Тип мероприятия:
                                <select
                                    className="event-time-input"
                                    name="format"
                                    value={this.state.format}
                                    onChange={this.handleChange}
                                >
                                    <option value="OFFLINE">Оффлайн</option>
                                    <option value="ONLINE">Онлайн</option>
                                </select>
                            </label>
                        </div>

                        <div className="card-buttons mt-4">
                            <button type="submit" className="event-button">
                                Создать мероприятие
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        );
    }
}

export default withNavigation(EventForm);