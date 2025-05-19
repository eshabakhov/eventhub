// ProfilePage.jsx
import React, {Component} from 'react';
import UserContext from '../../UserContext';
import {useNavigate} from 'react-router-dom';
import "../../css/ProfilePage.css";
import API_BASE_URL from "../../config";
import 'bootstrap-icons/font/bootstrap-icons.css';
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import api from '../common/AxiosInstance';

function ProfilePageWithNavigation(props) {
    const navigate = useNavigate();
    return <ProfilePage {...props} navigate={navigate}/>;
}

class ProfilePage extends Component {
    static contextType = UserContext;

    state = {
        formData: {},
        originalData: {},
        isDirty: false,
        loading: true,
        successMessage: '',
        sidebarOpen: false
    };

    sidebarRef = React.createRef();

    componentDidMount() {
    document.addEventListener("mousedown", this.handleClickOutside);
    api.get(`${API_BASE_URL}/auth/me`, {
        withCredentials: true,
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then((response) => {
        const data = response.data;

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

        this.setState({ formData, originalData: formData, isDirty: false, loading: false });
    })
    .catch((error) => {
        console.error('Ошибка при загрузке профиля:', error);
        this.setState({ loading: false });
    });
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
        const updatedFormData = { ...this.state.formData, [name]: value }
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
            await api.put(commonEndpoint, commonFields, {
                withCredentials: true,
                headers: { 'Content-Type': 'application/json' }
            });

            if (roleEndpoint) {
                await api.put(roleEndpoint, restFields, {
                    withCredentials: true,
                    headers: { 'Content-Type': 'application/json' }
                });
            }

            const meResponse = await api.get(`${API_BASE_URL}/auth/me`, {
                withCredentials: true,
                headers: { 'Content-Type': 'application/json' }
            });

            const data = meResponse.data;

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

            this.setState({
                formData: updatedFormData,
                originalData: updatedFormData,
                isDirty: false,
                successMessage: 'Профиль успешно обновлён',
            });

            setUser(data.user);

            setTimeout(() => this.setState({ successMessage: '' }), 3000);
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
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Мой профиль"
                    user={user}
                    navigate={navigate}/>

                <div className="main-content-wrapper">
                    <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                    <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={user}/>

                    <div className="profile-content">
                        <div className="profile-card">
                            {successMessage && <div className="success-message">{successMessage}</div>}
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
                                    <input className="profile-input" type="email" name="email"
                                           value={formData.email || ''}
                                           onChange={this.handleChange} disabled/>
                                </label>
                                <label className="profile-label">
                                    Логин:
                                    <input className="profile-input" type="text" name="username"
                                           value={formData.username}
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

                                <div className="button-wrapper">
                                    <button type="button" className={`cancel-button ${this.state.isDirty ? 'dirty' : ''}`} onClick={this.handleCancel}>
                                        Отмена
                                    </button>
                                    <button type="submit" className="profile-button">Сохранить</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default ProfilePageWithNavigation;