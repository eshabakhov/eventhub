// ProfilePage.jsx
import React, {Component} from 'react';
import UserContext from '../../UserContext';
import {useNavigate, useParams} from 'react-router-dom';
import "../../css/ProfilePage.css";
import "../../css/UserProfile.css";
import API_BASE_URL from "../../config";
import 'bootstrap-icons/font/bootstrap-icons.css';
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import api from '../common/AxiosInstance';


const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
}

function UserProfileWithNavigation(props) {
    const navigate = useNavigate();
    return <UserProfile {...props} navigate={navigate}/>;
}

function withParams(Component) {
    return props => <Component {...props} params={useParams()}/>;
}

class UserProfile extends Component {
    static contextType = UserContext;

    state = {
        formData: {},
        originalData: {},
        username: '',
        isDirty: false,
        loading: true,
        successMessage: '',
        sidebarOpen: false,
        eventsOpen: false,
        isOrganizersPath: false,
        isMembersPath: false
    };

    sidebarRef = React.createRef();

    componentDidMount() {
        const currentUrl = window.location.href;
        const isOrganizersPath = currentUrl.includes('/organizers/');
        const isMembersPath = currentUrl.includes('/users/');
        this.setState({isOrganizersPath, isMembersPath});
        if (isMembersPath) {
            const memberId = this.props.params;
            document.addEventListener("mousedown", this.handleClickOutside);
            api.get(`${API_BASE_URL}/v1/users/members/${memberId.id}`, {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then((response) => {
                    api.get(`${API_BASE_URL}/v1/users/${memberId.id}`, {
                        withCredentials: true,
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                        .then((response) => {
                            const data = response.data;
                            this.setState({username: data.username});
                        })
                        .catch((error) => {
                            console.error('Ошибка при загрузке профиля:', error);
                        });
                    const data = response.data;
                    const formData = {
                        lastName: data.lastName || '',
                        firstName: data.firstName || '',
                        patronymic: data.patronymic || '',
                        birthDate: data.birthDate || '',
                        birthCity: data.birthCity || '',
                        privacy: data.privacy || 'public',
                    };

                    this.setState({formData, originalData: formData, isDirty: false, loading: false});
                })
                .catch((error) => {
                    console.error('Ошибка при загрузке профиля:', error);
                    this.setState({loading: false});
                });
            api.get(`/v1/friends/${memberId.id}/isfriend`, {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then((response) => {
                    const data = response.data;
                    if (data.privacy === 'PUBLIC' || data.privacy === 'ONLY_FRIENDS' && data.friendly === true) {
                        this.setState({eventsOpen: true});
                    } else {
                        this.setState({eventsOpen: false});
                    }
                    console.log(data);
                })
                .catch((error) => {
                    console.error('Ошибка при загрузке профиля:', error);
                });
        } else {
            const organizer = this.props.params;
            document.addEventListener("mousedown", this.handleClickOutside);
            api.get(`${API_BASE_URL}/v1/users/organizers/${organizer.id}`, {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then((response) => {
                const data = response.data;
                console.log(data);
                const formData = {
                    name: data.name || '',
                    industry: data.industry || '',
                    description: data.description || '',
                    address: data.address || '',
                    isAccredited: data.isAccredited || '',
                };
                this.setState({formData, originalData: formData, isDirty: false, loading: false, eventsOpen: true});
                if (this.context.user.role === 'MEMBER') {
                    this.checkIsFavorite();
                }
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

    handleBack = () => {
        this.props.navigate('/events');
    };

    handleUserEvents = () => {
        console.log('кнопка')
        if (this.state.isOrganizersPath) {
            this.props.navigate(`/organizers/${this.props.params.id}/events`);
        } else {
            this.props.navigate(`/users/${this.props.params.id}/events`);
        }
        // ? this.props.navigate('/organizers') : this.props.navigate('/members');
        // const memberId = this.props.params;
        // this.props.navigate(`/users/${memberId.id}/events`);
    };

    handleAddOrgToFavourites = async () => {
        try {
            const organizer = this.props.params;
            const isFavorite = this.state.isFavorite;
            const member = this.context.user;
            const request = isFavorite
                ? await api.delete(`/v1/members/${member.id}/organizers/${organizer.id}`, {credentials: "include"})
                : await api.post(`/v1/members/${member.id}/organizers/${organizer.id}`, {credentials: "include"});

            this.setState({isFavorite : !isFavorite})
        }
        catch (err) {

        }
        finally {

        }
    }

    checkIsFavorite = async () => {
        try {
            const organizer = this.props.params;
            const member = this.context.user;
            const response = await api.get(`/v1/members/${member.id}/organizers/${organizer.id}`, {credentials: "include"});
            if (response.status === 200) {
                if (response.data)
                    this.setState({isFavorite: true})
            }
        }
        catch {

        }
        finally {

        }
    }

    render() {
        const {navigate} = this.props;
        const {user} = this.context;
        const {formData, loading, successMessage, sidebarOpen, username, eventsOpen,
            isMembersPath, isOrganizersPath, isFavorite} = this.state;

        if (loading) return <div className="profile-loading">Загрузка...</div>;
        console.log(eventsOpen)
        return (
            <div className="profile-page">
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title={username}
                    user={user}
                    navigate={navigate}/>

                <div className="main-content-wrapper">
                    <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                    <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={user}/>

                    <div className="profile-content">
                        <div className="profile-card">
                            {successMessage && <div className="success-message">{successMessage}</div>}
                            <form>
                                <>
                                    {isMembersPath && (
                                        <>
                                            <label className="profile-label">
                                                Фамилия:
                                                <input className="profile-input" type="text" name="lastName"
                                                       value={formData.lastName} disabled/>
                                            </label>
                                            <label className="profile-label">
                                                Имя:
                                                <input className="profile-input" type="text" name="firstName"
                                                       value={formData.firstName} disabled/>
                                            </label>
                                            <label className="profile-label">
                                                Отчество:
                                                <input className="profile-input" type="text" name="patronymic"
                                                       value={formData.patronymic} disabled/>
                                            </label>
                                            <label className="profile-label">
                                                Дата рождения:
                                                <input className="profile-input" type="date" name="birthDate"
                                                       value={formData.birthDate} disabled/>
                                            </label>
                                            <label className="profile-label">
                                                Город рождения:
                                                <input className="profile-input" type="text" name="birthCity"
                                                       value={formData.birthCity} disabled/>
                                            </label>
                                        </>
                                    )}
                                    {isOrganizersPath && (
                                        <>
                                            <label className="profile-label">
                                                <div className="accreditation-status">
                                                    {formData.isAccredited ? (
                                                        <span className="accredited">
                        <i className="bi bi-check-circle-fill text-success"></i> Аккредитованная организация
                    </span>
                                                    ) : (
                                                        <span className="not-accredited">
                        <i className="bi bi-check-circle-fill text-secondary"></i> Неаккредитованая организация
                    </span>
                                                    )}
                                                </div>
                                            </label>
                                            <label className="profile-label">
                                                Название организации:
                                                <input className="profile-input" type="text" name="name"
                                                       value={formData.name} disabled/>
                                            </label>
                                            <label className="profile-label">
                                                Описание организации:
                                                <input className="profile-input" type="text" name="description"
                                                       value={formData.description} disabled/>
                                            </label>
                                            <label className="profile-label">
                                                Сфера деятельности:
                                                <input className="profile-input" type="text" name="description"
                                                       value={formData.industry} disabled/>
                                            </label>
                                            <label className="profile-label">
                                                Адрес:
                                                <input className="profile-input" type="text" name="description"
                                                       value={formData.address} disabled/>
                                            </label>
                                        </>
                                    )}
                                </>
                                <div className={`button-wrapper ${isOrganizersPath ? 'two-buttons' : ''}`}>
                                    <button type="button" className={`${!eventsOpen ? 'no-hover' : 'user-profile-button'}`} onClick={this.handleUserEvents} disabled={!eventsOpen}>
                                        {`Мероприятия ${isMembersPath ? 'пользователя' : 'организации'}`}
                                    </button>
                                    {isOrganizersPath && user.role === 'MEMBER' ? (
                                        <button type="button" className={`${!eventsOpen ? 'no-hover' : `user-profile-button add-to-favorites-button ${isFavorite ? 'favorite' : ''}`}`} onClick={this.handleAddOrgToFavourites}>

                                            <div className="star-container">
                                                <svg
                                                    className={`star-icon ${isFavorite ? 'favorite' : ''}`}
                                                    viewBox="0 0 24 24"
                                                >
                                                    <path
                                                        d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                                                    />
                                                </svg>
                                            </div>

                                            {isFavorite ? `Удалить из избранного` : `Добавить в избранное`}
                                        </button>) : ''}

                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default withNavigation(withParams(UserProfile));