// ModeratorCreate.jsx
import React, { Component } from 'react';
import UserContext from '../../UserContext';
import { useNavigate } from 'react-router-dom';
import "../../css/ProfilePage.css";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";

function ModeratorCreateWithNavigation(props) {
    const navigate = useNavigate();
    return <ModeratorCreate {...props} navigate={navigate} />;
}

class ModeratorCreate extends Component {
    static contextType = UserContext;

    state = {
        formData: {},
        loading: true,
        successMessage: '',
        userId: ''
    };

    componentDidMount() {
        const { user } = this.context;

        const formData = {
            role: 'MODERATOR',
            email: '',
            username: '',
            displayName: '',
            password: '',
            isAdmin: false

        };
        this.setState({ formData, loading: false });
    }

    handleChange = (e) => {
        const { name, type, checked, value } = e.target;
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
        const {formData, userId} = this.state;

        const commonEndpoint = `http://localhost:9500/api/v1/users`;

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
            // 1. Создание пользователя
            const createResponse = await fetch(commonEndpoint, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                credentials: 'include',
                body: JSON.stringify(commonFields)
            });

            const createdUser = await createResponse.json();
            const userId = createdUser.id;

            // 2. Обновление ролевых данных
            const roleEndpoint = `http://localhost:9500/api/v1/users/moderators/${userId}`;

            await fetch(roleEndpoint, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                credentials: 'include',
                body: JSON.stringify(restFields)
            });
            this.setState({
                successMessage: 'Модератор успешно создан',
            });
            setTimeout(() => this.setState({successMessage: ''}), 3000);
            this.props.navigate('/moderator-management')

        } catch (error) {
            console.error('Ошибка при обновлении профиля:', error);
        }
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

    handleBack = () => {
        this.props.navigate('/moderator-management');
    };

    render() {
        const { navigate } = this.props;
        const { user } = this.context;
        const { formData, loading, successMessage, userId } = this.state;

        if (loading) return <div className="profile-loading">Загрузка...</div>;

        return (
            <div>
                <div className="header-bar">
                    <div className="top-logo" onClick={() => navigate("/events")} style={{ cursor: "pointer" }}>
                        <img src={EventHubLogo} alt="Logo" className="logo" />
                    </div>
                    <h1 className="friends-title">Создание модератора</h1>
                    <div className="login-button-container">
                        <ProfileDropdown navigate={navigate} />
                    </div>
                </div>
                <div className="profile-container">
                    <div className="back-area" onClick={this.handleBack}>
                        <button className="back-button">←</button>
                    </div>
                    <div className="profile-card">
                        <form onSubmit={this.handleSubmit}>

                            {/* Общие поля */}
                            <label className="profile-label">
                                Роль:
                                <input className="profile-input" type="text" name="role" value='Модератор' disabled />
                            </label>
                            <label className="profile-label">
                                Почта:
                                <input className="profile-input" type="email" name="email" value={formData.email || ''} onChange={this.handleChange} />
                            </label>
                            <label className="profile-label">
                                Логин:
                                <input className="profile-input" type="text" name="username" value={formData.username} onChange={this.handleChange} />
                            </label>
                            <label className="profile-label">
                                Отображаемое имя:
                                <input className="profile-input" type="text" name="displayName" value={formData.displayName} onChange={this.handleChange} />
                            </label>
                            <label className="profile-label">
                                Пароль:
                                <input className="profile-input" type="password" name="password" value={formData.password} onChange={this.handleChange} />
                            </label>

                            <div className="card-buttons mt-4">
                                <button type="submit" className="profile-button">Сохранить изменения</button>
                            </div>
                            {successMessage && <p className="success-message">{successMessage}</p>}
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

export default ModeratorCreateWithNavigation;