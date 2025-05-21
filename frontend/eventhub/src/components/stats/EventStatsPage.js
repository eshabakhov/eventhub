import React, { Component } from "react";
import api from "../common/AxiosInstance";
import Header from "../common/Header";
import {useNavigate} from "react-router-dom";
import SideBar from "../common/SideBar";
import "../../css/EventStatsPage.css";
import UserContext from "../../UserContext"; // Импорт стилей

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
};

class EventStatsPage extends Component {
    static contextType = UserContext;
    state = {
        events: [],
        users: [],
        selectedEventId: "",
        selectedUserId: "",
        eventPage: 1,
        userPage: 1,
        pageSize: 10,
        totalEventPages: 1,
        totalUserPages: 1,
        totalEventViews: null,
        totalUserViews: null,
        userEventViews: null,
        sidebarOpen: false,
        errorMessage: ""
    };
    sidebarRef = React.createRef();

    componentDidMount() {
        document.addEventListener("mousedown", this.handleClickOutside);
        this.fetchEvents();
        this.fetchUsers();
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

    fetchEvents = async () => {
        const { eventPage, pageSize } = this.state;
        try {
            const response = await api.get("/v1/events", {
                params: { page: eventPage, pageSize }
            });
            this.setState({
                events: response.data.list,
                totalEventPages: response.data.totalPages
            });
        } catch (error) {
            console.error("Ошибка при загрузке мероприятий:", error);
        }
    };

    fetchUsers = async () => {
        const { userPage, pageSize } = this.state;
        try {
            const response = await api.get("/v1/users", {
                params: { page: userPage, pageSize }
            });
            this.setState({
                users: response.data.list,
                totalUserPages: response.data.totalPages
            });
        } catch (error) {
            console.error("Ошибка при загрузке пользователей:", error);
        }
    };

    handleChange = (e) => {
        const { name, value } = e.target;
        this.setState({ [name]: value }, this.fetchStats);
    };

    fetchStats = async () => {
        const { selectedEventId, selectedUserId } = this.state;

        this.setState({
            totalEventViews: null,
            totalUserViews: null,
            userEventViews: null,
            errorMessage: ""
        });

        try {
            if (selectedEventId) {
                const res = await api.get(`/v1/stats/event/${selectedEventId}/views`);
                this.setState({ totalEventViews: res.data });
            }

            if (selectedUserId) {
                const res = await api.get(`/v1/stats/user/${selectedUserId}/views`);
                this.setState({ totalUserViews: res.data });
            }

            if (selectedEventId && selectedUserId) {
                const res = await api.get(`/v1/stats/event/${selectedEventId}/user/${selectedUserId}/views`);
                this.setState({ userEventViews: res.data });
            }
        } catch (error) {
            console.error("Ошибка при загрузке статистики:", error);
            this.setState({ errorMessage: "Не удалось загрузить статистику." });
        }
    };

    handlePageSizeChange = (e) => {
        const newSize = parseInt(e.target.value);
        this.setState(
            {
                pageSize: newSize,
                eventPage: 0,
                userPage: 0
            },
            () => {
                this.fetchEvents();
                this.fetchUsers();
            }
        );
    };

    handleEventPageChange = (direction) => {
        this.setState(
            (prevState) => ({ eventPage: prevState.eventPage + direction }),
            this.fetchEvents
        );
    };

    handleUserPageChange = (direction) => {
        this.setState(
            (prevState) => ({ userPage: prevState.userPage + direction }),
            this.fetchUsers
        );
    };

    render() {
        const {navigate} = this.props;
        const {
            events,
            users,
            selectedEventId,
            selectedUserId,
            eventPage,
            userPage,
            pageSize,
            totalEventPages,
            totalUserPages,
            totalEventViews,
            totalUserViews,
            userEventViews,
            sidebarOpen,
            errorMessage
        } = this.state;

        return (
            <div className="stats-page-container">
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Статистика просмотров"
                    navigate={navigate}/>
                <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>
                <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={this.context.user}/>

                <div className="stats-controls">
                    <div className="page-size-selector">
                        <label>Размер:</label>
                        <select value={pageSize} onChange={this.handlePageSizeChange}>
                            <option value={5}>5</option>
                            <option value={10}>10</option>
                            <option value={25}>25</option>
                            <option value={50}>50</option>
                        </select>
                    </div>

                    <div className="stats-selector">
                        <label>Мероприятие:</label>
                        <select name="selectedEventId" value={selectedEventId} onChange={this.handleChange}>
                            <option value="">-- Выберите мероприятие --</option>
                            {events.map((event) => (
                                <option key={event.id} value={event.id}>
                                    {event.title}
                                </option>
                            ))}
                        </select>
                        <div className="pagination-controls">
                            <button
                                type="button"
                                className="back-forward"
                                onClick={() => this.handleEventPageChange(-1)}
                                disabled={eventPage <= 0}
                            >
                                Назад
                            </button>
                            <span>
                                {eventPage}
                            </span>
                            <button
                                type="button"
                                className="back-forward"
                                onClick={() => this.handleEventPageChange(1)}
                                disabled={eventPage + 1 >= totalEventPages}
                            >
                                Далее
                            </button>
                        </div>
                    </div>

                    <div className="stats-selector">
                        <label>Пользователь:</label>
                        <select name="selectedUserId" value={selectedUserId} onChange={this.handleChange}>
                            <option value="">-- Выберите пользователя --</option>
                            {users.map((user) => (
                                <option key={user.id} value={user.id}>
                                    {user.displayName} ({user.username})
                                </option>
                            ))}
                        </select>
                        <div className="pagination-controls">
                            <button
                                type="button"
                                className="back-forward"
                                onClick={() => this.handleUserPageChange(-1)}
                                disabled={userPage <= 0}
                            >
                                Назад
                            </button>
                            <span>
                                {userPage}
                            </span>
                            <button
                                type="button"
                                className="back-forward"
                                onClick={() => this.handleUserPageChange(1)}
                                disabled={userPage + 1 >= totalUserPages}
                            >
                                Далее
                            </button>
                        </div>
                    </div>
                </div>

                <div className="stats-results">
                    {selectedEventId && totalEventViews !== null && (
                        <div className="stat-card">
                            <div className="stat-label">Общее количество просмотров мероприятия</div>
                            <div className="stat-value">{totalEventViews}</div>
                        </div>
                    )}
                    {selectedUserId && totalUserViews !== null && (
                        <div className="stat-card">
                            <div className="stat-label">Общее количество просмотров от пользователя</div>
                            <div className="stat-value">{totalUserViews}</div>
                        </div>
                    )}
                    {selectedEventId && selectedUserId && userEventViews !== null && (
                        <div className="stat-card">
                            <div className="stat-label">Просмотры выбранного мероприятия этим пользователем</div>
                            <div className="stat-value">{userEventViews}</div>
                        </div>
                    )}
                    {errorMessage && <div className="error-message">{errorMessage}</div>}
                </div>
            </div>
        );
    }
}

export default withNavigation(EventStatsPage);