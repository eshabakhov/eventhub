import React from 'react';
import { useParams } from 'react-router-dom';
import { withNavigation } from "../events/EventsPage";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import "../../css/EventEdit.css";
import ProfileDropdown from "../profile/ProfileDropdown";

export function withParams(Component) {
    return props => <Component {...props} params={useParams()} />;
}

const formatDateForBackend = (dateTimeString) => {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return `${date.getDate().toString().padStart(2, '0')}/` +
        `${(date.getMonth() + 1).toString().padStart(2, '0')}/` +
        `${date.getFullYear()} ` +
        `${date.getHours().toString().padStart(2, '0')}:` +
        `${date.getMinutes().toString().padStart(2, '0')}`;
};

const formatDateForInput = (dateString) => {
    if (!dateString) return '';
    const [date, time] = dateString.split(' ');
    const [day, month, year] = date.split('/');
    return `${year}-${month}-${day}T${time}`;
};

class EventEdit extends React.Component {
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
            tags: [],
            newTag: '',
            errors: {},
            isEditing: false
        };
    }

    componentDidMount() {
        console.log('ok')
        const { eventId } = this.props.params;
        console.log(eventId)

        if (eventId) {
            fetch(`http://localhost:9500/api/v1/events/${eventId}`, {
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            })
                .then(res => {
                    if (!res.ok) throw new Error('Ошибка загрузки');
                    return res.json();
                })
                .then(data => {
                    this.setState({
                        ...data,
                        startDateTime: formatDateForInput(data.startDateTime),
                        endDateTime: formatDateForInput(data.endDateTime),
                        tags: data.tags || [],
                        isEditing: true
                    });
                })
                .catch(err => console.error('Ошибка получения данных мероприятия:', err));
        }
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

    handleTagInputChange = (e) => {
        this.setState({ newTag: e.target.value });
    };

    handleAddTag = (e) => {
        e.preventDefault();
        const { newTag, tags } = this.state;
        const { eventId } = this.props.params;

        if (newTag.trim() && !tags.some(t => t.name === newTag.trim())) {
            const updatedTags = [...tags, { name: newTag.trim() }];

            fetch(`http://localhost:9500/api/v1/events/${eventId}/tag`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    tags: [{ name: newTag.trim() }]
                })
            })
                .then(res => {
                    if (!res.ok) throw new Error('Ошибка добавления тега');
                    return res.json();
                })
                .then(() => {
                    this.setState({
                        tags: updatedTags,
                        newTag: ''
                    });
                })
                .catch(err => console.error('Ошибка при добавлении тега:', err));
        }
    };

    handleRemoveTag = (tagToRemove) => {
        const { eventId } = this.props.params;

        fetch(`http://localhost:9500/api/v1/events/${eventId}/tag`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                    id: tagToRemove.id,
                    name: tagToRemove.name
            })
        })
            .then(res => {
                if (!res.ok) throw new Error('Ошибка удаления тега');
                this.setState(prevState => ({
                    tags: prevState.tags.filter(tag => tag.id !== (tagToRemove.id || tagToRemove))
                }));
            })
            .catch(err => console.error('Ошибка при удалении тега:', err));
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
        const { navigate } = this.props;
        const { eventId } = this.props.params;

        if (this.validate()) {
            const eventData = {
                title: this.state.title,
                description: this.state.description,
                shortDescription: this.state.shortDescription,
                location: this.state.location,
                startDateTime: formatDateForBackend(this.state.startDateTime),
                endDateTime: formatDateForBackend(this.state.endDateTime),
                format: this.state.format,
                organizerId: user.id,
                tags: this.state.tags
            };

            const method = this.state.isEditing ? 'PATCH' : 'POST';
            const url = this.state.isEditing
                ? `http://localhost:9500/api/v1/events/${eventId}`
                : 'http://localhost:9500/api/v1/events';

            fetch(url, {
                method,
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(eventData)
            })
                .then(res => {
                    if (!res.ok) throw new Error('Ошибка при сохранении');
                    navigate('/my-events');
                })
                .catch(err => console.error('Ошибка сохранения:', err));
        }
    };

    render() {
        const { errors, isEditing, tags, newTag } = this.state;
        const { navigate } = this.props;

        return (
            <div className="events-f">
                <div className="header-bar">
                    <div className="top-logo">
                        <img src={EventHubLogo} alt="Logo" className="logo" />
                    </div>
                    <label className="panel-title">Редактирование мероприятия</label>
                    <div className="login-button-container">
                        <button className="create-button" onClick={() => navigate("/my-events")}>
                            Мои мероприятия
                        </button>
                        <ProfileDropdown navigate={navigate} />
                    </div>
                </div>
                <div className="event-form">
                    <form onSubmit={this.handleSubmit}>
                        {[
                            { name: "title", label: "Название мероприятия" },
                            { name: "description", label: "Описание" },
                            { name: "shortDescription", label: "Краткое описание" },
                            { name: "location", label: "Локация" },
                        ].map(({ name, label }) => (
                            <div className="form-group" key={name}>
                                <label className="event-label">
                                    {label}:
                                    <input
                                        className="event-input"
                                        type="text"
                                        name={name}
                                        value={this.state[name]}
                                        onChange={this.handleChange}
                                    />
                                    {errors[name] && <span className="error-message">{errors[name]}</span>}
                                </label>
                            </div>
                        ))}

                        {/* Поле для добавления тегов */}
                        <div className="form-group">
                            <label className="event-label">
                                Теги:
                                <div className="tags-input-container">
                                    <input
                                        type="text"
                                        className="event-input"
                                        value={newTag}
                                        onChange={this.handleTagInputChange}
                                        placeholder="Добавьте тег"
                                    />
                                    <button
                                        className="add-tag-button"
                                        onClick={this.handleAddTag}
                                    >
                                        Добавить
                                    </button>
                                </div>
                                <div className="tags-container">
                                    {tags.map((tag, index) => (
                                        <span key={tag.id || index} className="tag">
                                                {tag.name || tag}
                                            <button
                                                type="button"
                                                className="tag-remove"
                                                onClick={() => this.handleRemoveTag(tag)}
                                            >
                                                    ×
                                                </button>
                                            </span>
                                    ))}
                                </div>
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
                                {errors.startDateTime && <span className="error-message">{errors.startDateTime}</span>}
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
                                {isEditing ? 'Сохранить изменения' : 'Создать мероприятие'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        );
    }
}

export default withNavigation(withParams(EventEdit));