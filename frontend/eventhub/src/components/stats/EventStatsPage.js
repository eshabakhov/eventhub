import React, { Component } from "react";
import api from "../common/AxiosInstance";

class EventStatsPage extends Component {
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
        errorMessage: ""
    };

    componentDidMount() {
        this.fetchEvents();
        this.fetchUsers();
    }

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
            errorMessage
        } = this.state;

        return (
            <div className="view-stats-page">
                <h2>Статистика просмотров</h2>

                <div style={{ marginBottom: 10 }}>
                    <label>Размер страницы:</label>
                    <select value={pageSize} onChange={this.handlePageSizeChange}>
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={25}>25</option>
                        <option value={50}>50</option>
                    </select>
                </div>

                <div style={{ marginBottom: 20 }}>
                    <label>Мероприятие:</label>
                    <select name="selectedEventId" value={selectedEventId} onChange={this.handleChange}>
                        <option value="">-- Выберите мероприятие --</option>
                        {events.map((event) => (
                            <option key={event.id} value={event.id}>
                                {event.title}
                            </option>
                        ))}
                    </select>
                    <div>
                        <button
                            type="button"
                            onClick={() => this.handleEventPageChange(-1)}
                            disabled={eventPage <= 0}
                        >
                            Назад
                        </button>
                        <span style={{ margin: "0 10px" }}>
                            Страница {eventPage + 1} из {totalEventPages}
                        </span>
                        <button
                            type="button"
                            onClick={() => this.handleEventPageChange(1)}
                            disabled={eventPage + 1 >= totalEventPages}
                        >
                            Далее
                        </button>
                    </div>
                </div>

                <div style={{ marginBottom: 20 }}>
                    <label>Пользователь:</label>
                    <select name="selectedUserId" value={selectedUserId} onChange={this.handleChange}>
                        <option value="">-- Выберите пользователя --</option>
                        {users.map((user) => (
                            <option key={user.id} value={user.id}>
                                {user.displayName} ({user.username})
                            </option>
                        ))}
                    </select>
                    <div>
                        <button
                            type="button"
                            onClick={() => this.handleUserPageChange(-1)}
                            disabled={userPage <= 0}
                        >
                            Назад
                        </button>
                        <span style={{ margin: "0 10px" }}>
                            Страница {userPage + 1} из {totalUserPages}
                        </span>
                        <button
                            type="button"
                            onClick={() => this.handleUserPageChange(1)}
                            disabled={userPage + 1 >= totalUserPages}
                        >
                            Далее
                        </button>
                    </div>
                </div>

                <div className="stats-result">
                    {selectedEventId && totalEventViews !== null && (
                        <p>Общее количество просмотров мероприятия: <strong>{totalEventViews}</strong></p>
                    )}
                    {selectedUserId && totalUserViews !== null && (
                        <p>Общее количество просмотров от пользователя: <strong>{totalUserViews}</strong></p>
                    )}
                    {selectedEventId && selectedUserId && userEventViews !== null && (
                        <p>Просмотры выбранного мероприятия этим пользователем: <strong>{userEventViews}</strong></p>
                    )}
                    {errorMessage && <p style={{ color: "red" }}>{errorMessage}</p>}
                </div>
            </div>
        );
    }
}

export default EventStatsPage;
