import React from 'react';
import {useParams, useNavigate} from 'react-router-dom';
import {withNavigation} from "../events/EventsPage";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import "../../css/EventForm.css";
import axios from "axios";
import ProfileDropdown from "../profile/ProfileDropdown";


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
        const {description, location, startDateTime, endDateTime} = this.state;
        const errors = {};

        if (!description) errors.description = 'Описание обязательно';
        if (!location) errors.location = 'Локация обязательна';
        if (!startDateTime) errors.startDateTime = 'Дата начала обязательна';
        if (!endDateTime) errors.endDateTime = 'Дата окончания обязательна';

        if (startDateTime && endDateTime && new Date(startDateTime) > new Date(endDateTime)) {
            errors.endDateTime = 'Дата окончания должна быть после даты начала';
        }

        this.setState({errors});
        return Object.keys(errors).length === 0;
    };

    handleSubmit = (e) => {
        console.log('handleSubmit');
        e.preventDefault();
        const {user} = this.context;

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
                    this.props.navigate('/my-events');
                })
                .catch(error => {
                    console.error('Error saving event:', error);
                });


        }
    };

    render() {
        const {title, description, shortDescription, location, errors} = this.state;
        const {navigate} = this.props;

        return (
            <div>
                <div className="header-bar">
                    <div className="top-logo">
                        <img src={EventHubLogo} alt="Logo" className="logo"/>
                    </div>
                    <label className="panel-title">Создание мероприятия</label>
                    <div className="return-button-container">
                        <button className="create-button" onClick={() => navigate("/my-events")}>
                            Мои мероприятия
                        </button>
                        <ProfileDropdown navigate={navigate}/>
                    </div>
                </div>

                <div className="event-form-container">
                    <div className="back-area" onClick={this.handleBack}>
                        <button className="back-button">←</button>
                    </div>
                    <div className="event-form-card">
                        <form onSubmit={this.handleSubmit}>
                            <label className="event-form-label">
                                Название:
                                <input className="event-form-input" type="text" name="title" value={title}
                                       onChange={this.handleChange}/>
                            </label>
                            <label className="event-form-label">
                                Описание:
                                <textarea className="event-form-input" name="description" value={description}
                                          onChange={this.handleChange}/>
                            </label>
                            <label className="event-form-label">
                                Краткое описание:
                                <input className="event-form-input" type="text" name="shortDescription"
                                       value={shortDescription} onChange={this.handleChange}/>
                            </label>
                            <label className="event-edit-label">
                                Локация:
                                <input className="event-form-input" type="text" name="location" value={location}
                                       onChange={this.handleChange}/>
                            </label>

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
                                    {errors.startDateTime &&
                                        <span className="error-message">{errors.startDateTime}</span>}
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
                                        className="event-format-input"
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
            </div>
        );
    }
}

export default withNavigation(EventForm);