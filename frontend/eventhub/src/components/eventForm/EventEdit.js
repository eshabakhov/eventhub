import React from 'react';
import {useParams} from 'react-router-dom';
import {withNavigation} from "../events/EventsPage";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import "../../css/EventEdit.css";
import ProfileDropdown from "../profile/ProfileDropdown";
import API_BASE_URL from "../../config";

export function withParams(Component) {
    return props => <Component {...props} params={useParams()}/>;
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
            files: [],
            newTag: '',
            errors: {},
            isEditing: false,
            selectedFile: null,
            uploadedFiles: []
        };
    }

    componentDidMount() {
        console.log('ok')
        const {eventId} = this.props.params;
        console.log(eventId)

        if (eventId) {
            fetch(`${API_BASE_URL}/v1/events/${eventId}`, {
                credentials: 'include',
                headers: {'Accept': 'application/json'}
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
                        files: data.files || [],
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
        this.setState({newTag: e.target.value});
    };

    handleAddTag = async (e) => {
        e.preventDefault();
        const { newTag, tags } = this.state;
        const { eventId } = this.props.params;

        if (newTag.trim() && !tags.some(t => t.name === newTag.trim())) {
            try {
                const response = await fetch(`${API_BASE_URL}/v1/events/${eventId}/tag`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({
                        tags: [{ name: newTag.trim() }]
                    })
                });

                if (!response.ok) {
                    throw new Error('Ошибка добавления тега');
                }

                const result = await response.json();

                const addedTag = Array.isArray(result) ? result[0] : result;

                this.setState(prevState => ({
                    tags: [...prevState.tags, addedTag], // Добавляем тег с ID
                    newTag: ''
                }));

                console.log('Тег успешно добавлен:', addedTag);
            } catch (err) {
                console.error('Ошибка при добавлении тега:', err);
                // Можно добавить отображение ошибки пользователю
                this.setState({ error: 'Не удалось добавить тег' });
            }
        }
    };

    handleRemoveTag = (tagToRemove) => {
        const {eventId} = this.props.params;

        fetch(`${API_BASE_URL}/v1/events/${eventId}/tag`, {
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

    handleRemoveFile = (fileToRemove) => {
        const {eventId} = this.props.params;

        fetch(`${API_BASE_URL}/v1/events/${eventId}/eventFiles`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                fileId: fileToRemove.fileId,
                fileName: fileToRemove.fileName
            })
        })
            .then(res => {
                if (!res.ok) throw new Error('Ошибка удаления файла');
                this.setState(prevState => ({
                    files: prevState.files.filter(file => file.fileId !== (fileToRemove.fileId || fileToRemove))
                }));
            })
            .catch(err => console.error('Ошибка при удалении файла:', err));
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
        e.preventDefault();
        const {user} = this.context;
        const {navigate} = this.props;
        const {eventId} = this.props.params;

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
                ? `${API_BASE_URL}/v1/events/${eventId}`
                : `${API_BASE_URL}/v1/events`;

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
                })
                .catch(err => console.error('Ошибка сохранения:', err));
        }
    };



    handleSelectFile = () => {
        this.fileInputRef.click();
    };

    handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            this.setState({selectedFile: file});
        }
    };

    handleUploadFile = () => {
        const {selectedFile} = this.state;
        const {eventId} = this.props.params;

        if (!selectedFile) {
            alert("Сначала выберите файл");
            return;
        }

        const reader = new FileReader();
        reader.onload = async () => {
            const fileContentBase64 = reader.result.split(',')[1];

            const eventFileDTO = {
                eventId: parseInt(eventId),
                fileName: selectedFile.name,
                fileType: selectedFile.type,
                fileSize: selectedFile.size,
                fileContent: fileContentBase64
            };

            let addedFile = {
                fileId: null,
                fileName: undefined
            };

            try {
                const res = await fetch(`http://localhost:9500/api/v1/events/${eventId}/eventFiles`, {
                    method: "POST",
                    credentials: "include",
                    headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                    body: JSON.stringify(eventFileDTO)
                });

                if (!res.ok) throw new Error("Ошибка при загрузке файла");


                addedFile.fileId = await res.json(); // Получаем данные ответа
                addedFile.fileName = eventFileDTO.fileName;



                this.setState(prevState => ({
                    files: [...prevState.files, addedFile],
                    selectedFile: null
                }));

                alert("Файл успешно загружен!");
                this.setState({selectedFile: null});
            } catch (err) {
                console.error("Ошибка при загрузке файла:", err);
                alert("Не удалось загрузить файл.");
            }
        };

        reader.readAsDataURL(selectedFile);
    };

    handleBack = () => {
        this.props.navigate('/my-events');
    };

    render() {
        const {title, description, shortDescription, location, errors, isEditing, tags, newTag, files} = this.state;
        const {navigate} = this.props;

        return (
            <div>
                <div className="header-bar">
                    <div className="top-logo">
                        <img src={EventHubLogo} alt="Logo" className="logo"/>
                    </div>
                    <label className="panel-title">Редактирование мероприятия</label>
                    <div className="login-button-container">
                        <button className="create-button" onClick={() => navigate("/my-events")}>
                            Мои мероприятия
                        </button>
                        <ProfileDropdown navigate={navigate}/>
                    </div>
                </div>
                <div className="event-edit-container">
                    <div className="back-area" onClick={this.handleBack}>
                        <button className="back-button">←</button>
                    </div>
                    <div className="event-edit-card">
                        <form onSubmit={this.handleSubmit}>

                            <label className="event-edit-label">
                                Название:
                                <input className="event-edit-input" type="text" name="title" value={title}
                                       onChange={this.handleChange}/>
                            </label>
                            <label className="event-edit-label">
                                Описание:
                                <textarea className="event-edit-input" name="description" value={description}
                                          onChange={this.handleChange}/>
                            </label>
                            <label className="event-edit-label">
                                Краткое описание:
                                <input className="event-edit-input" type="text" name="shortDescription"
                                       value={shortDescription} onChange={this.handleChange}/>
                            </label>
                            <label className="event-edit-label">
                                Локация:
                                <input className="event-edit-input" type="text" name="location" value={location}
                                       onChange={this.handleChange}/>
                            </label>

                            {/* Поле для добавления тегов */}

                            <label className="event-edit-label">
                                Теги:
                                <div className="event-edit-input">
                                    <input
                                        type="text"
                                        className="event-input"
                                        value={newTag}
                                        onChange={this.handleTagInputChange}
                                        placeholder="Добавьте тег"
                                    />
                                    <button
                                        className="event-edit-button"
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


                            <div className="file-upload-buttons">
                                <input
                                    type="file"
                                    accept="*/*"
                                    style={{display: "none"}}
                                    ref={(ref) => (this.fileInputRef = ref)}
                                    onChange={this.handleFileChange}
                                />
                                <button
                                    type="button"
                                    className="event-edit-button"
                                    onClick={this.handleSelectFile}
                                >
                                    Прикрепить файл
                                </button>

                                <button
                                    type="button"
                                    className="event-edit-button"
                                    onClick={this.handleUploadFile}
                                    disabled={!this.state.selectedFile}
                                >
                                    ⬆️ Загрузить
                                </button>
                                {this.state.selectedFile && (
                                    <div className="selected1-file-info">
                                        Файл: <strong>{this.state.selectedFile.name}</strong>
                                    </div>
                                )}
                            </div>

                            {/* Поле для добавления файлов */}
                            <div className="form-group">
                                <label className="event-label">
                                    <div className="files-input-container">
                                    </div>
                                    <div className="files-container">
                                        {files.map((file, index) => (
                                            <span key={file.fileId || index} className="file">
                                                {file.fileName || file}
                                                <button
                                                    type="button"
                                                    className="file-remove"
                                                    onClick={() => this.handleRemoveFile(file)}
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
                                        <option value="OFFLINE">Офлайн</option>
                                        <option value="ONLINE">Онлайн</option>
                                    </select>
                                </label>
                            </div>

                            <div className="card-buttons mt-4">
                                <button type="submit" className="event-edit-button">
                                    Сохранить изменения
                                </button>
                            </div>


                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

export default withNavigation(withParams(EventEdit));