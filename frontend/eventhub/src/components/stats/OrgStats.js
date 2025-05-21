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

class OrgStats extends Component {
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
        userEventViews: null,
        sidebarOpen: false,
        errorMessage: '',
        totalFavoriteCount: null,
        totalUserViews: null,
        totalMembers: null,
        totalEvents: null,
    };
    sidebarRef = React.createRef();

    componentDidMount() {
        document.addEventListener("mousedown", this.handleClickOutside);
        this.fetchStats();
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
        const { selectedEventId, selectedUserId} = this.state;

        const user = this.context.user;

        this.setState({
            totalEventViews: null,
            totalUserViews: null,
            userEventViews: null,
            errorMessage: "",
            totalFavoriteCount: null,
            totalMembers: null,
            totalEvents: null,
        });

        try {
            let res = await api.get(`/v1/stats/organizers/${user.id}/events`);
            this.setState({ totalEvents: res.data });

            res = await api.get(`/v1/stats/organizers/${user.id}/members`);
            this.setState({ totalMembers: res.data });

            res = await api.get(`/v1/stats/organizers/${user.id}/views`);
            this.setState({ totalUserViews: res.data });

            res = await api.get(`/v1/stats/organizers/${user.id}/favorites`);
            this.setState({ totalFavoriteCount: res.data });



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
            totalFavoriteCount,
            totalUserViews,
            totalMembers,
            totalEvents,
            sidebarOpen,
            errorMessage
        } = this.state;

        return (
            <div className="stats-page-container">
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Статистика организации"
                    navigate={navigate}/>

                <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={this.context.user}/>

                <div className="stats-results">
                    {totalUserViews !== null && (
                        <div className="stat-card">
                            <div className="stat-label">Общее количество просмотров мероприятий</div>
                            <div className="stat-value">{totalUserViews}</div>
                        </div>
                    )}
                    {totalEvents !== null && (
                        <div className="stat-card">
                            <div className="stat-label">Количество мероприятий</div>
                            <div className="stat-value">{totalEvents}</div>
                        </div>
                    )}
                    {totalMembers !== null && (
                        <div className="stat-card">
                            <div className="stat-label">Участников за всё время</div>
                            <div className="stat-value">{totalMembers}</div>
                        </div>
                    )}
                    {totalFavoriteCount !== null && (
                        <div className="stat-card">
                            <div className="stat-label">В избранном у пользователей</div>
                            <div className="stat-value">{totalFavoriteCount}</div>
                        </div>
                    )}
                    {errorMessage && <div className="error-message">{errorMessage}</div>}
                </div>
            </div>
        );
    }
}

export default withNavigation(OrgStats);