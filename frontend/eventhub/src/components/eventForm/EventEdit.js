import React, {useEffect} from 'react';
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
import leaflet from "leaflet";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import offlineIconImg from "../../img/offline-marker.png";
import onlineIconImg from "../../img/online-marker.png";
import {GeoSearchControl, OpenStreetMapProvider} from 'leaflet-geosearch';
import {MapContainer, Marker, Popup, TileLayer, useMap, useMapEvents} from "react-leaflet";
// Иконки для маркеров
const onlineIcon = new leaflet.Icon({
    iconUrl: onlineIconImg,
    shadowUrl: iconShadow,
    iconSize: [41, 41],
    iconAnchor: [12, 41],
});

const offlineIcon = new leaflet.Icon({
    iconUrl: offlineIconImg,
    shadowUrl: iconShadow,
    iconSize: [41, 41],
    iconAnchor: [12, 41],
});


// Компонент для центрирования карты
function CenterMap({center, zoom}) {
    const map = useMap();
    useEffect(() => {
        map.setView(center, zoom);
    }, [center, zoom]);
    return null;
}

function MapClickHandler({onClick}) {
    const map = useMapEvents({
        click: (e) => {
            onClick(e.latlng);
        },
    });
    return null;
}

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
            latitude: null,
            longitude: null,
            startDateTime: '',
            endDateTime: '',
            format: 'OFFLINE',
            organizerId: '',
            tags: [],
            files: [],
            newTag: '',
            errors: {},
            isEditing: false,
            isCreating: false,
            selectedFiles: [],
            uploadedFiles: [],
            isSuccess: false,
            eventId: null,
            mapKey: Date.now() // Для принудительного перерисовывания карты
        };
        this.mapRef = React.createRef();
    }


    // Новые методы
    getFileIcon = (fileName) => {
        const extension = fileName.split('.').pop().toLowerCase();
        switch (extension) {
            case 'pdf':
                return 'bi-file-earmark-pdf';
            case 'doc':
                return 'bi bi-filetype-doc';
            case 'docx':
                return 'bi-filetype-docx';
            case 'xls':
                return 'bi-filetype-xls'
            case 'xlsx':
                return 'bi-filetype-xlsx'
            case 'ppt':
                return 'bi-filetype-ppt';
            case 'pptx':
                return 'bi-filetype-pptx';
            case 'jpg':
                return 'bi-filetype-jpg';
            case 'jpeg':
                return 'bi-file-earmark-image';
            case 'png':
                return 'bi-filetype-png';
            case 'gif':
                return 'bi-filetype-gif';
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
                        isEditing: true,
                        isCreating: false
                    });
                })
                .catch(err => console.error('Ошибка получения данных мероприятия:', err));
        } else {
            const {user} = this.context;
            console.log(user);
            this.setState({
                isCreating: true,
                isEditing: false,
                location: user.organizerAddress,
            });
            this.geocodeAddress(user.organizerAddress);
        }
    }

    componentWillUnmount() {
        document.removeEventListener("mousedown", this.handleClickOutside);
    }

    handleChange = (e) => {
        const {name, value} = e.target;

        if (name === 'format' && value === 'ONLINE') {
            this.setState({
                [name]: value,
                latitude: null,
                longitude: null,
                errors: {
                    ...this.state.errors,
                    location: null
                }
            });
        } else {
            this.setState({
                [name]: value,
                errors: {
                    ...this.state.errors,
                    [name]: null
                }
            });

            // Если изменилось поле location и формат офлайн, пробуем геокодировать
            if (name === 'location' && this.state.format === 'OFFLINE' && value) {
                this.geocodeAddress(value);
            }
        }
    };

    // Геокодирование адреса
    geocodeAddress = async (address) => {
        try {
            const provider = new OpenStreetMapProvider();
            const results = await provider.search({query: address});

            if (results.length > 0) {
                const {x: lng, y: lat} = results[0];
                this.setState({
                    latitude: lat,
                    longitude: lng,
                    mapKey: Date.now() // Обновляем ключ карты для перерисовки
                });
            }
        } catch (err) {
            console.error('Ошибка геокодирования:', err);
        }
    };

    // Обработка клика по карте
    handleMapClick = async (latlng) => {
        if (this.state.format !== 'OFFLINE') return;

        const {lat, lng} = latlng;

        try {
            const response = await fetch(
                `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`
            );
            const data = await response.json();

            // Если полный адрес не доступен, формируем его из доступных частей
            const parts = [];
            if (data.address?.country) parts.push(data.address.country);
            if (data.address?.city) parts.push(data.address.city);
            if (data.address?.road) parts.push(data.address.road);
            if (data.address?.house_number) parts.push(data.address.house_number);

            let address = parts.join(', ');

            this.setState({
                latitude: lat,
                longitude: lng,
                location: address || `${lat.toFixed(6)}, ${lng.toFixed(6)}`
            });
        } catch (err) {
            console.error('Ошибка обратного геокодирования:', err);
            this.setState({
                latitude: lat,
                longitude: lng,
                location: `${lat.toFixed(6)}, ${lng.toFixed(6)}`
            });
        }
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

    AddTags = (eventId) => {
        const {newTag, tags} = this.state;
        tags.map((tag, index) => {
            this.sendAddTagRequest(tag, eventId);
        })
    }

    sendAddTagRequest = async (tagName, eventId) => {
        const response = await fetch(`${API_BASE_URL}/v1/tags/events/${eventId}`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({name: tagName})
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
        console.log('Тег успешно добавлен: ', addedTag);
    }

    handleAddTag = async (e) => {
        e?.preventDefault();
        const {newTag, tags} = this.state;
        const {eventId} = this.props.params;

        if (newTag.trim() && !tags.some(t => t.name === newTag.trim())) {
            try {
                if (!eventId) {
                    this.setState(prevState => ({
                        tags: [...prevState.tags, newTag.trim()],
                        newTag: ''
                    }));
                    return;
                }
                await this.sendAddTagRequest(newTag.trim(), eventId);
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
        const {eventId} = this.props.params || this.state.eventId;
        const {isEditing, isCreating} = this.state;
        const eventData = {
            title: this.state.title,
            description: this.state.description,
            shortDescription: this.state.shortDescription,
            location: this.state.location,
            longitude: this.state.longitude,
            latitude: this.state.latitude,
            startDateTime: formatDateForBackend(this.state.startDateTime),
            endDateTime: formatDateForBackend(this.state.endDateTime),
            format: this.state.format,
            organizerId: user.id,
        };

        if (this.validate()) {
            if (isCreating) {
                console.log('handleSubmit');
                console.log('Отправка данных мероприятия:', eventData);
                // Здесь можно добавить вызов API для сохранения мероприятия
                // this.props.onSubmit(eventData);
                fetch(`${API_BASE_URL}/v1/events`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(eventData)
                })
                    .then(async response => {
                        if (!response.ok) throw new Error('Network response was not ok');
                        let event = await response.json();
                        this.setState({eventId: event.id});
                        this.AddTags(event.id);
                        await this.uploadFiles(event.id, this.state.selectedFiles);
                        this.props.navigate('/my-events');
                    })
                    .catch(error => {
                        console.error('Error saving event:', error);
                    });

            } else {
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
                        this.setState({isSuccess: true})
                        document.scrollingElement.scrollTo(0, 0)
                    })
                    .catch(err => console.error('Ошибка сохранения:', err));
            }
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
            await this.uploadFiles(eventId, selectedFiles);

        } catch (err) {
            console.error("Ошибка при загрузке файлов:", err);
            this.setState({
                showConfirmModal: true,
                mainText: "Не удалось загрузить некоторые файлы"
            });
        }
    };

    uploadFiles = async (eventId, selectedFiles) => {
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
            tags, newTag, files, showConfirmModal, mainText, sideBarOpen, isSuccess
        } = this.state;
        const {navigate} = this.props;
        const position = this.state.latitude && this.state.longitude
            ? [this.state.latitude, this.state.longitude]
            : [55.75, 37.61];
        const zoom = 18;

        return (
            <div className='page-container'>
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

                <form onSubmit={this.handleSubmit} className="event-edit-container">
                    <div className={`msg ok_msg ${!isSuccess ? 'hidden' : ''}`}>
                        <div role="alert" className="msg_text">
                            <b>Изменения сохранены</b>
                        </div>
                    </div>
                    <div className="event-edit-card">
                        <label className="event-edit-label">
                            Название:
                            <input required className="event-edit-input" type="text" name="title" value={title}
                                   onChange={this.handleChange}/>
                        </label>
                        <label className="event-edit-label">
                            Описание:
                            <textarea required className="event-edit-input" name="description" value={description}
                                      onChange={this.handleChange}/>
                        </label>
                        <label className="event-edit-label">
                            Краткое описание:
                            <input required className="event-edit-input" type="text" name="shortDescription"
                                   value={shortDescription} onChange={this.handleChange}/>
                        </label>

                        <div className="event-edit-row">
                            <label className="event-label">
                                Дата начала:
                                <input required
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
                                <input required
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
                                <select required
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

                        {this.state.format === 'OFFLINE' && (
                            <>
                                <label className="event-edit-label">
                                    Место проведения:
                                    <input required
                                           className="event-edit-input"
                                           type="text"
                                           name="location"
                                           value={this.state.location}
                                           onChange={this.handleChange}
                                    />
                                </label>

                                <div className="map-container">
                                    <MapContainer
                                        key={this.state.mapKey}
                                        center={position}
                                        zoom={zoom}
                                        style={{height: '300px', borderRadius: '8px'}}
                                    >
                                        <TileLayer
                                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                                        />
                                        <CenterMap center={position} zoom={zoom}/>
                                        <MapClickHandler onClick={this.handleMapClick}/>
                                        {this.state.latitude && this.state.longitude && (
                                            <Marker
                                                position={[this.state.latitude, this.state.longitude]}
                                                icon={offlineIcon}
                                            >
                                                <Popup>{this.state.location}</Popup>
                                            </Marker>
                                        )}
                                    </MapContainer>
                                </div>
                            </>
                        )}

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
                                        className={`event-edit-button upload-button ${this.state.isCreating ? 'hidden' : ''}`}
                                        onClick={this.handleUploadFiles}
                                    >
                                        <i className="bi bi-upload"></i> Загрузить все файлы
                                    </button>
                                </div>
                            )}
                        </div>

                        <div className="event-edit-card-buttons">
                            <button type="cancel" onClick={this.handleBack} className="event-edit-button cancel">
                                Отмена
                            </button>

                            <button type="submit" className="event-edit-button save">
                                Сохранить
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        );
    }
}

export default withNavigation(withParams(EventEdit));