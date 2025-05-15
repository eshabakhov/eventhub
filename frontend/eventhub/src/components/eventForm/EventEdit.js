import React from 'react';
import {useParams} from 'react-router-dom';
import {withNavigation} from "../events/EventsPage";
import UserContext from "../../UserContext";
import EventHubLogo from "../../img/eventhub.png";
import "../../css/EventEdit.css";
import ProfileDropdown from "../profile/ProfileDropdown";
import API_BASE_URL from "../../config";
import ConfirmModal from "../common/ConfirmModal";
import Header from "../common/Header";
import SideBar from "../common/SideBar";

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
            selectedFiles: [],
            uploadedFiles: []
        };
    }

    // Новые методы
    getFileIcon = (fileName) => {
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
    sidebarRef = React.createRef();

    componentDidMount() {
        console.log('ok')
        const {eventId} = this.props.params;
        console.log(eventId)
        document.addEventListener("mousedown", this.handleClickOutside);

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

    componentWillUnmount() {
        document.removeEventListener("mousedown", this.handleClickOutside);
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

    toggleSidebar = () => {
        this.setState(prev => ({sidebarOpen: !prev.sidebarOpen}));
    };

    handleClickOutside = (event) => {
        if (this.state.sidebarOpen &&
            this.sidebarRef.current &&
            !this.sidebarRef.current.contains(event.target) &&
            !event.target.classList.contains('burger-button') &&
            !event.target.closest('.burger-button')) {
            this.setState({sidebarOpen: false});
        }
    };

    handleTagInputChange = (e) => {
        this.setState({newTag: e.target.value});
    };

    handleAddTag = async (e) => {
        e.preventDefault();
        const {newTag, tags} = this.state;
        const {eventId} = this.props.params;

        if (newTag.trim() && !tags.some(t => t.name === newTag.trim())) {
            try {
                const response = await fetch(`${API_BASE_URL}/v1/tags/events/${eventId}`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({
                        tags: [{name: newTag.trim()}]
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
                this.setState({error: 'Не удалось добавить тег'});
            }
        }
    };

    handleRemoveTag = (tagToRemove) => {
        const {eventId} = this.props.params;

        fetch(`${API_BASE_URL}/v1/tags/${tagToRemove.id}/events/${eventId}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
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
        const files = Array.from(e.target.files);
        if (files && files.length > 0) {
            this.setState(prevState => ({
                selectedFiles: [...prevState.selectedFiles, ...files]
            }));
        }
    };

    handleRemoveSelectedFile = (index) => {
        this.setState(prev => ({
            selectedFiles: prev.selectedFiles.filter((_, i) => i !== index)
        }));
    };

    formatFileSize = (bytes) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    };

    handleUploadFiles = async () => {
        const {selectedFiles} = this.state;
        const {eventId} = this.props.params;

        if (!selectedFiles || selectedFiles.length === 0) {
            this.setState({
                showConfirmModal: true,
                mainText: "Сначала выберите файлы"
            });
            return;
        }

        try {
            const uploadPromises = selectedFiles.map(file => {
                return new Promise((resolve, reject) => {
                    const reader = new FileReader();
                    reader.onload = async () => {
                        const fileContentBase64 = reader.result.split(',')[1];

                        const eventFileDTO = {
                            fileName: file.name,
                            fileType: file.type,
                            fileSize: file.size,
                            fileContent: fileContentBase64
                        };

                        try {
                            const res = await fetch(`${API_BASE_URL}/v1/events/${eventId}/eventFiles`, {
                                method: "POST",
                                credentials: "include",
                                headers: {
                                    "Content-Type": "application/json",
                                    "Accept": "application/json"
                                },
                                body: JSON.stringify(eventFileDTO)
                            });

                            if (!res.ok) throw new Error("Ошибка при загрузке файла");

                            const fileId = await res.json();
                            resolve({fileId, fileName: file.name});
                        } catch (err) {
                            reject(err);
                        }
                    };
                    reader.readAsDataURL(file);
                });
            });

            const results = await Promise.all(uploadPromises);

            this.setState(prev => ({
                files: [...prev.files, ...results],
                selectedFiles: [],
                showConfirmModal: true,
                mainText: `Успешно загружено ${results.length} файлов`
            }));

        } catch (err) {
            console.error("Ошибка при загрузке файлов:", err);
            this.setState({
                showConfirmModal: true,
                mainText: "Не удалось загрузить некоторые файлы"
            });
        }
    };

    handleBack = () => {
        this.props.navigate('/my-events');
    };

    handleCloseModal = () => {
        this.setState({
            showConfirmModal: false,
            mainText: ""
        });
    };

    render() {
        const {
            title, description, shortDescription, location, errors, isEditing,
            tags, newTag, files, showConfirmModal, mainText, sideBarOpen
        } = this.state;
        const {navigate} = this.props;

        return (
            <div>
                <ConfirmModal
                    isOpen={showConfirmModal}
                    mainText={mainText}
                    okText="Ок"
                    onClose={this.handleCloseModal}
                />

                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Редактирование мероприятия"
                    user={this.context.user}
                    navigate={navigate}
                />
                <div className={`sidebar-overlay ${this.state.sidebarOpen ? 'active' : ''}`}></div>
                <SideBar user={this.context.user} sidebarRef={this.sidebarRef} sidebarOpen={this.state.sidebarOpen}/>

                <div className="event-edit-container">
                    <div className="event-edit-card">
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

                        <div className="event-edit-row">
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

                        {/* Поле для тегов */}
                        <label className="event-edit-label">
                            Теги:
                            <div className="tags-input-container">
                                <input
                                    type="text"
                                    className="event-input tag-input"
                                    value={newTag}
                                    onChange={this.handleTagInputChange}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter')
                                            this.handleAddTag(e);
                                    }}
                                    placeholder="Добавьте тег"
                                />
                                <div className="add-tag-button">
                                    <i onClick={this.handleAddTag} className="bi bi-check-circle-fill"></i>
                                </div>
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

                        {/* Поле для файлов */}
                        <div className="files-section">
                            <h3 className="files-title">Прикрепленные файлы</h3>

                            {files.length > 0 && (
                                <div className="uploaded-files-container">
                                    {files.map((file, index) => (
                                        <div key={file.fileId || index} className="uploaded-file">
                                            <div className="file-icon-name">
                                                <i className={`bi ${this.getFileIcon(file.fileName)}`}></i>
                                                <span className="file-name">{file.fileName || file}</span>
                                            </div>
                                            <button
                                                type="button"
                                                className="file-remove"
                                                onClick={() => this.handleRemoveFile(file)}
                                            >
                                                <i className="bi bi-trash"></i>
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}

                            <div
                                id="dropZone"
                                className="drop-zone"
                                onClick={this.handleSelectFile}
                                onDragOver={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    e.currentTarget.classList.add("dragover");
                                }}
                                onDragLeave={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    e.currentTarget.classList.remove("dragover");
                                }}
                                onDrop={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    e.currentTarget.classList.remove("dragover");

                                    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                                        this.setState(prevState => ({
                                            selectedFiles: [...prevState.selectedFiles, ...Array.from(e.dataTransfer.files)]
                                        }));
                                        e.dataTransfer.clearData();
                                    }
                                }}
                            >
                                <input
                                    type="file"
                                    id="fileInput"
                                    style={{display: 'none'}}
                                    ref={(ref) => (this.fileInputRef = ref)}
                                    onChange={this.handleFileChange}
                                    multiple
                                />
                                <div className="drop-zone-content">
                                    <i className="bi bi-cloud-arrow-up"></i>
                                    <p>Перетащите файлы сюда или кликните для выбора</p>
                                    <small>Можно выбрать несколько файлов</small>
                                </div>
                            </div>

                            {this.state.selectedFiles && this.state.selectedFiles.length > 0 && (
                                <div className="selected-files-container">
                                    <h4>Выбранные файлы:</h4>
                                    {this.state.selectedFiles.map((file, index) => (
                                        <div key={index} className="selected-file">
                                            <span className="file-name">{file.name}</span>
                                            <span className="file-size">({this.formatFileSize(file.size)})</span>
                                            <button
                                                type="button"
                                                className="file-remove"
                                                onClick={() => this.handleRemoveSelectedFile(index)}
                                            >
                                                <i className="bi bi-x-circle"></i>
                                            </button>
                                        </div>
                                    ))}
                                    <button
                                        type="button"
                                        className="event-edit-button upload-button"
                                        onClick={this.handleUploadFiles}
                                    >
                                        <i className="bi bi-upload"></i> Загрузить все файлы
                                    </button>
                                </div>
                            )}
                        </div>

                        {/*<div>*/}
                        {/*    <input*/}
                        {/*        type="file"*/}

                        {/*        style={{display: "none"}}*/}
                        {/*        ref={(ref) => (this.fileInputRef = ref)}*/}
                        {/*        onChange={this.handleFileChange}*/}
                        {/*    />*/}
                        {/*    <button*/}
                        {/*        type="button"*/}
                        {/*        className="event-edit-button"*/}
                        {/*        onClick={this.handleSelectFile}*/}
                        {/*    >*/}
                        {/*        <i className="bi bi-paperclip"></i> Прикрепить файл*/}
                        {/*    </button>*/}

                        {/*    <button*/}
                        {/*        type="button"*/}
                        {/*        className="event-edit-button"*/}
                        {/*        onClick={this.handleUploadFile}*/}
                        {/*        disabled={!this.state.selectedFile}*/}
                        {/*    ><i className="bi bi-upload"></i> Загрузить*/}
                        {/*    </button>*/}
                        {/*    {this.state.selectedFile && (*/}
                        {/*        <div>*/}
                        {/*            Файл: <strong>{this.state.selectedFile.name}</strong>*/}
                        {/*        </div>*/}
                        {/*    )}*/}
                        {/*</div>*/}

                        {/* Поле для добавления файлов */}
                        {/*<label className="event-label">*/}
                        {/*    <div className="files-container">*/}
                        {/*        {files.map((file, index) => (*/}
                        {/*            <span key={file.fileId || index} className="file">*/}
                        {/*                <i className="bi bi-file-earmark-fill"></i>*/}
                        {/*                {file.fileName || file}*/}
                        {/*                <button*/}
                        {/*                    type="button"*/}
                        {/*                    className="file-remove"*/}
                        {/*                    onClick={() => this.handleRemoveFile(file)}*/}
                        {/*                >*/}
                        {/*                            ×*/}
                        {/*                        </button>*/}
                        {/*                    </span>*/}
                        {/*        ))}*/}
                        {/*    </div>*/}
                        {/*</label>*/}
                        {/*<div className="form-group">*/}
                        {/*    <label>Attachments</label>*/}
                        {/*    <div id="dropZone" className="drop-zone">*/}
                        {/*        <input type="file" id="fileInput" multiple style={{display: 'inline'}}/>*/}
                        {/*        или перетащите его сюда*/}
                        {/*    </div>*/}
                        {/*    <div className="note">Upload up to 3 Files. Max File Size: 10 MB</div>*/}
                        {/*</div>*/}

                        <div className="event-edit-card-buttons">
                            <button type="cancel" onClick={this.handleBack} className="event-edit-button cancel">
                                Отмена
                            </button>

                            <button onClick={this.handleSubmit} className="event-edit-button save">
                                Сохранить
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default withNavigation(withParams(EventEdit));