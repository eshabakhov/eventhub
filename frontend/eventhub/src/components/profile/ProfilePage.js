// ProfilePage.jsx
import React, {Component} from 'react';
import UserContext from '../../UserContext';
import {useNavigate} from 'react-router-dom';
import "../../css/ProfilePage.css";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";
import API_BASE_URL from "../../config";
import 'bootstrap-icons/font/bootstrap-icons.css';

function ProfilePageWithNavigation(props) {
    const navigate = useNavigate();
    return <ProfilePage {...props} navigate={navigate}/>;
}

class ProfilePage extends Component {
    static contextType = UserContext;

    state = {
        formData: {},
        loading: true,
        successMessage: '',
        sidebarOpen: false
    };

    sidebarRef = React.createRef();

    componentDidMount() {
        const {user} = this.context;
        document.addEventListener("mousedown", this.handleClickOutside);

        if (user && user.id) {
            fetch(`${API_BASE_URL}/auth/me`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(async (response) => {
                    if (!response.ok) {
                        throw new Error(`Ошибка HTTP: ${response.status}`);
                    }
                    const data = await response.json();

                    const formData = {
                        role: data.user.role || '',
                        email: data.user.email || '',
                        username: data.user.username || '',
                        displayName: data.user.displayName || '',
                        password: '',
                        ...(data.customUser && {
                            organizationName: data.customUser.name || '',
                            description: data.customUser.description || '',
                            industry: data.customUser.industry || '',
                            address: data.customUser.address || '',
                            accreditation: data.customUser.isAccredited || '',
                            lastName: data.customUser.lastName || '',
                            firstName: data.customUser.firstName || '',
                            patronymic: data.customUser.patronymic || '',
                            birthDate: data.customUser.birthDate || '',
                            birthCity: data.customUser.birthCity || '',
                            privacy: data.customUser.privacy || 'public',
                            isAdmin: !!data.customUser.isAdmin
                        })
                    };

                    this.setState({formData, loading: false});
                })
                .catch((error) => {
                    console.error('Ошибка при загрузке профиля:', error);
                    this.setState({loading: false});
                });
        }
    }

    componentWillUnmount() {
        document.removeEventListener("mousedown", this.handleClickOutside);
    }

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

    handleChange = (e) => {
        const {name, type, checked, value} = e.target;
        this.setState((prevState) => ({
            formData: {
                ...prevState.formData,
                [name]: type === 'checkbox' ? checked : value,
            }
        }));
    };

    handleSubmit = async (e) => {
        e.preventDefault();
        const {user, setUser} = this.context;
        const {formData} = this.state;

        const rolePathMap = {
            ORGANIZER: 'organizers',
            MEMBER: 'members',
            MODERATOR: 'moderators'
        };

        const rolePath = rolePathMap[user.role];
        const commonEndpoint = `${API_BASE_URL}/v1/users/${user.id}`;
        const roleEndpoint = rolePath
            ? `${API_BASE_URL}/v1/users/${rolePath}/${user.id}`
            : null;

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
            // 1. Обновление общих данных
            const commonResponse = await fetch(commonEndpoint, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                credentials: 'include',
                body: JSON.stringify(commonFields)
            });

            if (!commonResponse.ok) {
                throw new Error(`Ошибка при обновлении общих данных: ${commonResponse.status}`);
            }

            // 2. Обновление ролевых данных
            if (roleEndpoint) {
                const roleResponse = await fetch(roleEndpoint, {
                    method: 'PUT',
                    headers: {'Content-Type': 'application/json'},
                    credentials: 'include',
                    body: JSON.stringify(restFields)
                });

                if (!roleResponse.ok) {
                    throw new Error(`Ошибка при обновлении ролевых данных: ${roleResponse.status}`);
                }
            }

            // 3. Загрузка обновлённых данных
            const meResponse = await fetch(`${API_BASE_URL}/auth/me`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!meResponse.ok) {
                throw new Error(`Ошибка при получении данных пользователя: ${meResponse.status}`);
            }

            const data = await meResponse.json();

            const updatedFormData = {
                role: data.user.role || '',
                email: data.user.email || '',
                username: data.user.username || '',
                displayName: data.user.displayName || '',
                password: '',
                ...(data.customUser && {
                    organizationName: data.customUser.name || '',
                    description: data.customUser.description || '',
                    industry: data.customUser.industry || '',
                    address: data.customUser.address || '',
                    accreditation: data.customUser.isAccredited || '',
                    lastName: data.customUser.lastName || '',
                    firstName: data.customUser.firstName || '',
                    patronymic: data.customUser.patronymic || '',
                    birthDate: data.customUser.birthDate || '',
                    birthCity: data.customUser.birthCity || '',
                    privacy: data.customUser.privacy || 'public',
                    isAdmin: !!data.customUser.isAdmin
                })
            };

            // 4. Обновление состояния и контекста
            this.setState({
                formData: updatedFormData,
                successMessage: 'Профиль успешно обновлён'
            });

            setUser(data.user);

            setTimeout(() => this.setState({successMessage: ''}), 3000);
        } catch (error) {
            console.error('Ошибка при обновлении профиля:', error);
        }
    };

    handleBack = () => {
        this.props.navigate('/events');
    };

    getRoleDisplayName = (role) => {
        switch (role) {
            case 'MEMBER':
                return 'Участник';
            case 'ORGANIZER':
                return 'Организатор';
            case 'MODERATOR':
                return 'Модератор';
            default:
                return 'Неизвестно';
        }
    };

    render() {
        const {navigate} = this.props;
        const {user} = this.context;
        const {formData, loading, successMessage, sidebarOpen} = this.state;

        if (loading) return <div className="profile-loading">Загрузка...</div>;

        return (
            <div className="profile-page">
                <div className="header-bar">
                    <div className="burger-button" onClick={this.toggleSidebar}>
                        <i className="bi bi-list" style={{fontSize: '24px'}}></i>
                    </div>
                    <div className="top-logo" onClick={() => navigate("/events")}>
                        <img src={EventHubLogo} alt="Logo" className="logo"/>
                    </div>
                    <h1 className="friends-title">Мой профиль</h1>
                    <ProfileDropdown navigate={navigate}/>
                </div>

                <div className="main-content-wrapper">
                    <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}
                         onClick={this.toggleSidebar}></div>

                    <div className={`profile-sidebar ${sidebarOpen ? 'open': 'closed'}`} ref={this.sidebarRef}>
                        <ul>
                            <li onClick={() => {
                                navigate("/profile");
                                this.setState({sidebarOpen: false});
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
                                        <i className="bi bi-clipboard-check-fill"></i> Аккредитация мероприятий
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

                    <div className="profile-content">
                        <div className="profile-card">
                            <form onSubmit={this.handleSubmit}>
                                {user.role === 'ORGANIZER' && (
                                    <div className="profile-label">
                                        {formData.accreditation ? (
                                            <span
                                                className="accreditation-status accredited">Организация аккредитована</span>
                                        ) : (
                                            <span className="accreditation-status not-accredited">Организация не аккредитована</span>
                                        )}
                                    </div>
                                )}

                                {/* Общие поля */}
                                <label className="profile-label">
                                    Роль:
                                    <input className="profile-input" type="text" name="role"
                                           value={this.getRoleDisplayName(formData.role)} disabled/>
                                </label>
                                <label className="profile-label">
                                    Почта:
                                    <input className="profile-input" type="email" name="email" value={formData.email || ''}
                                           onChange={this.handleChange} disabled/>
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

                                {/* Организатор */}
                                {user.role === 'ORGANIZER' && (
                                    <>
                                        <label className="profile-label">
                                            Название:
                                            <input className="profile-input" type="text" name="organizationName"
                                                   value={formData.organizationName} onChange={this.handleChange}/>
                                        </label>
                                        <label className="profile-label">
                                            Описание:
                                            <textarea className="profile-input" name="description"
                                                      value={formData.description} onChange={this.handleChange}/>
                                        </label>
                                        <label className="profile-label">
                                            Сфера деятельности:
                                            <input className="profile-input" type="text" name="industry"
                                                   value={formData.industry} onChange={this.handleChange}/>
                                        </label>
                                        <label className="profile-label">
                                            Адрес:
                                            <input className="profile-input" type="text" name="address"
                                                   value={formData.address} onChange={this.handleChange}/>
                                        </label>
                                    </>
                                )}

                                {/* Участник */}
                                {user.role === 'MEMBER' && (
                                    <>
                                        <label className="profile-label">
                                            Фамилия:
                                            <input className="profile-input" type="text" name="lastName"
                                                   value={formData.lastName} onChange={this.handleChange}/>
                                        </label>
                                        <label className="profile-label">
                                            Имя:
                                            <input className="profile-input" type="text" name="firstName"
                                                   value={formData.firstName} onChange={this.handleChange}/>
                                        </label>
                                        <label className="profile-label">
                                            Отчество:
                                            <input className="profile-input" type="text" name="patronymic"
                                                   value={formData.patronymic} onChange={this.handleChange}/>
                                        </label>
                                        <label className="profile-label">
                                            Дата рождения:
                                            <input className="profile-input" type="date" name="birthDate"
                                                   value={formData.birthDate} onChange={this.handleChange}/>
                                        </label>
                                        <label className="profile-label">
                                            Город рождения:
                                            <input className="profile-input" type="text" name="birthCity"
                                                   value={formData.birthCity} onChange={this.handleChange}/>
                                        </label>
                                    </>
                                )}

                                <button type="submit" className="profile-button">Сохранить</button>

                                {successMessage && <div className="success-message">{successMessage}</div>}
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default ProfilePageWithNavigation;