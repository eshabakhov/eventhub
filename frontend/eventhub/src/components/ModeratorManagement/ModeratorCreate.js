// ModeratorCreate.jsx
import React, {Component} from 'react';
import UserContext from '../../UserContext';
import {useNavigate} from 'react-router-dom';
import "../../css/ModeratorCreate.css";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";
import API_BASE_URL from "../../config";
import api from "../common/AxiosInstance";
import Header from "../common/Header";
import {withNavigation} from "../events/EventsPage";
import {withParams} from "../eventForm/EventEdit";


class ModeratorCreate extends Component {
    static contextType = UserContext;

    constructor(props) {
        super(props);
        this.state = {
            formData: {},
            originalData: {},
            isDirty: false,
            loading: true,
            successMessage: '',
            moderatorId: '',
            isEditing: false,
            isCreate: false
        };
    }

    componentDidMount() {
        const {moderatorId} = this.props.params;

        if (moderatorId) {
            const commonEndpoint = `${API_BASE_URL}/v1/users/${moderatorId}`;
            api.get(commonEndpoint, {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then((response) => {
                const data = response.data;
                console.log(data);

                const formData = {
                    role: data.role || '',
                    email: data.email || '',
                    username: data.username || '',
                    displayName: data.displayName || '',
                    password: '',
                };
                this.setState({
                    formData,
                    originalData: formData,
                    isDirty: false,
                    loading: false,
                    isEditing: true,
                    isCreate: false
                });
            }).catch((error) => {
                console.error('Ошибка при загрузке профиля:', error);
                this.setState({loading: false});
            });
        } else {
            const formData = {
                role: 'MODERATOR',
                email: '',
                username: '',
                displayName: '',
                password: '',
                isAdmin: false
            };
            this.setState({
                formData,
                originalData: formData,
                isDirty: false,
                loading: false,
                isEditing: false,
                isCreate: true
            });
        }
    }

    handleChange = (e) => {
        const {name, type, checked, value} = e.target;
        const updatedFormData = {...this.state.formData, [name]: value}
        const isDirty = JSON.stringify(updatedFormData) !== JSON.stringify(this.state.originalData);
        this.setState((prevState) => ({
            formData: {
                ...prevState.formData,
                [name]: type === 'checkbox' ? checked : value,
            },
            isDirty
        }));
    };

    handleCancel = () => {
        this.setState((prevState) => ({
            formData: {...prevState.originalData},
            isDirty: false
        }));

        setTimeout(this.handleBack, 500);
    };

    handleSubmit = async (e) => {
        e.preventDefault();
        const {formData} = this.state;

        const commonEndpoint = `${API_BASE_URL}/v1/users`;

        const {
            role,
            email,
            username,
            displayName,
            password,
            ...restFields
        } = formData;

        const commonFields = {
            role,
            email,
            username,
            displayName,
            password
        };

        try {

            if (this.state.isCreate) {
                // 1. Создание пользователя
                const createResponse = await api.post(commonEndpoint, commonFields, {
                    headers: {'Content-Type': 'application/json'},
                    credentials: 'include',
                });

                const createdUser = createResponse.data;
                const userId = createdUser.id;

                // 2. Обновление ролевых данных
                await api.put(`/v1/users/moderators/${userId}`, restFields, {
                    headers: {'Content-Type': 'application/json'},
                    credentials: 'include',
                });
                this.setState({
                    successMessage: 'Модератор успешно создан',
                });
                setTimeout(() => this.setState({successMessage: ''}), 3000);
                this.props.navigate('/moderator-management')
            } else {
                const {moderatorId} = this.props.params;
                await api.put(`${commonEndpoint}/${moderatorId}`, commonFields, {
                    headers: {'Content-Type': 'application/json'},
                    credentials: 'include',
                }).then((response) => {
                    const data = response.data;

                    const updatedFormData = {
                        role: data.role || '',
                        email: data.email || '',
                        username: data.username || '',
                        displayName: data.displayName || '',
                        password: ''
                    };

                    this.setState({
                        formData: updatedFormData,
                        originalData: updatedFormData,
                        isDirty: false,
                        successMessage: 'Информация обновлена успешно обновлена',
                    });

                    setTimeout(() => {
                            this.setState({successMessage: ''});
                            this.handleBack();
                        }
                        , 2000);

                }).catch((error) => {
                    console.error('Ошибка при обновлении модератора:', error);
                    this.setState({loading: false});
                });
            }


        } catch (error) {
            console.error('Ошибка при обновлении профиля:', error);
        }
    };

    handleBack = () => {
        this.props.navigate('/moderator-management');
    };

    render() {
        const {navigate} = this.props;
        const {formData, loading, successMessage} = this.state;

        if (loading) return <div className="profile-loading">Загрузка...</div>;

        return (
            <div>

                <Header
                    title="Создание модератора"
                    user={this.context.user}
                    navigate={navigate}
                    burgerVisible={false}
                />

                <div className="moderator-create-container">
                    <div className="moderator-create-card">
                        {successMessage && <div className="success-message">{successMessage}</div>}
                        <form onSubmit={this.handleSubmit}>
                            {/* Общие поля */}
                            <label className="profile-label">
                                Роль:
                                <input className="profile-input" type="text" name="role" value='Модератор' disabled/>
                            </label>
                            <label className="profile-label">
                                Почта:
                                <input className="profile-input" type="email" name="email" value={formData.email || ''}
                                       onChange={this.handleChange}/>
                            </label>
                            <label className="profile-label">
                                Логин:
                                <input className="profile-input" type="text" name="username" value={formData.username}
                                       onChange={this.handleChange}/>
                            </label>
                            <label className="profile-label">
                                Отображаемое имя:
                                <input className="profile-input" type="text" name="displayName"
                                       value={formData.displayName} onChange={this.handleChange}/>
                            </label>
                            <label className="profile-label">
                                Пароль:
                                <input className="profile-input" type="password" name="password"
                                       value={formData.password} onChange={this.handleChange}/>
                            </label>

                            <div className="button-wrapper">
                                <button type="button" className={`cancel-button ${this.state.isDirty ? 'dirty' : ''}`}
                                        onClick={this.handleCancel}>
                                    Отмена
                                </button>
                                <button type="submit" className="profile-button">Сохранить</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

export default withNavigation(withParams(ModeratorCreate));