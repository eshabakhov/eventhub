import React, {Component} from 'react';
import '../../css/FriendsPage.css';
import {useNavigate} from "react-router-dom";
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import UserContext from "../../UserContext";
import ConfirmModal from "../common/ConfirmModal";
import CurrentUser from "../common/CurrentUser";
import api from "../common/AxiosInstance";
import Pagination from "../common/Pagination";

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
};

class FriendsPage extends Component {
    static contextType = UserContext;

    state = {
        friends: [],
        friendsSearchResults: [],
        userId: null,
        searchQuery: '',
        searchResults: [],
        incomingRequests: [],
        sentRequests: [],
        sidebarOpen: false,
        nonFriends: [],
        isLoading: false,
        currentFriendsPage: 1,
        friendsPerPage: 10,
        totalFriends: 0,
        currentUsersPage: 1,
        usersPerPage: 10,
        totalUsers: 0,
    };
    sidebarRef = React.createRef();

    componentDidMount() {
        document.addEventListener("mousedown", this.handleClickOutside);
        CurrentUser.fetchCurrentUser(this.context.setUser);
        this.handleSearch(1);
        this.fetchFriends(1);
        this.fetchIncomingRequest();
        this.fetchOutgoingRequests();
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

    fetchFriends = (page) => {
        const {friendsPerPage} = this.state;
        api.get(`/v1/friends/list?page=${page}&pageSize=${friendsPerPage}`, {
            credentials: 'include',
        })
            .then(res => res.data)
            .then(data => {
                this.setState({
                    friends: data.list || [],
                    totalFriends: data.total,
                    currentFriendsPage: page});
            })
            .catch(console.error);
    };

    fetchIncomingRequest = () => {
        api.get(`/v1/friends/incoming`, {
            credentials: 'include',
        })
            .then(res => res.data)
            .then(data => {
                this.setState({incomingRequests: data});
            })
            .catch(console.error);
    };

    fetchOutgoingRequests = () => {
        api.get(`/v1/friends/outgoing   `, {
            credentials: 'include',
        })
            .then(res => res.data)
            .then(data => {
                this.setState({sentRequests: data});
            })
            .catch(console.error);
    };

    handleSearch = (page) => {
        const {searchQuery, usersPerPage} = this.state;

        api.get(`/v1/friends/?page=${page}&pageSize=${usersPerPage}&search=${encodeURIComponent(searchQuery)}`, {
            credentials: 'include',
        })
            .then(res => res.data)
            .then(async data => {
                // НЕ кладём пока в searchResults, чтобы не показывать пользователей без статуса дружбы
                this.setState({ totalUsers: data.total, currentUsersPage: page, searchResults: [], friendsSearchResults: [] });

                const resultsWithFriendStatus = await Promise.all(
                    data.list.map(async (user) => {
                        try {
                            const res = await api.get(`/v1/friends/${user.id}/isfriend`, { credentials: 'include' });
                            return { ...user, isFriend: res.data.friendly };
                        } catch (err) {
                            console.error(err);
                            return { ...user, isFriend: false };
                        }
                    })
                );

                // Устанавливаем окончательный список пользователей, с учётом дружбы
                this.setState({
                    searchResults: resultsWithFriendStatus,
                    friendsSearchResults: resultsWithFriendStatus.filter(u => u.isFriend),
                });
            })
            .catch(err => {
                console.error(err);
                this.setState({searchResults: [], friendsSearchResults: []});
            });
    };


    handleInputChange = (e) => {
        this.setState({searchQuery: e.target.value});
    };

    handleCloseModal = () => {
        this.setState({
            showConfirmModal: false,
            mainText: ""
        });
    };

    handleAddFriend = (recipientId) => {
        const {searchResults} = this.state;

        api.post(`/v1/friends/${recipientId}/send`, {
            credentials: 'include',
        })
            .then(() => {
                const recipient = searchResults.find(user => user.id === recipientId);
                if (recipient) {
                    this.setState(prevState => ({
                        sentRequests: [...prevState.sentRequests, recipient],
                    }));
                }

                this.setState({
                    showConfirmModal: true,
                    mainText: "Запрос отправлен"
                });
            })
            .catch(console.error);
    };

    handleRemoveFriend = (friendId) => {
        api.delete(`/v1/friends/${friendId}/remove`, {
            credentials: 'include',
        })
            .then(() => {
                this.fetchFriends(1)
                this.fetchIncomingRequest()
                this.fetchOutgoingRequests()
            })
            .catch(console.error);
        this.fetchFriends(this.state.friendsPerPage);
        this.handleSearch(this.state.currentUsersPage);
    };

    handleAcceptRequest = (senderId) => {
        api.post(`/v1/friends/${senderId}/accept`, {
            credentials: 'include',
        })
            .then(() => {
                this.fetchFriends(1)
                this.fetchIncomingRequest()
                this.fetchOutgoingRequests()
            })
            .catch(console.error);
        this.fetchFriends(this.state.friendsPerPage);
        this.handleSearch(this.state.currentUsersPage);
    };

    handleUsersPageClick = (pageNumber) => {
        this.handleSearch(pageNumber);
        this.setState({
            currentUsersPage: pageNumber,
        });
    };

    handleFriendsPageClick = (pageNumber) => {
        this.fetchFriends(pageNumber);
        this.setState({
            currentFriendsPage: pageNumber,
        });
    };

    render() {
        const {navigate} = this.props;
        const {
            friends,
            searchQuery,
            searchResults,
            incomingRequests,
            sentRequests,
            sidebarOpen,
            showConfirmModal,
            friendsPerPage,
            totalFriends,
            currentFriendsPage,
            usersPerPage,
            totalUsers,
            currentUsersPage,
            mainText
        } = this.state;
        const totalUsersPages = Math.ceil(totalUsers / usersPerPage);
        const totalFriendsPages = Math.ceil(totalFriends / friendsPerPage);
        return (

            <div className="events-container">
                <ConfirmModal
                    isOpen={showConfirmModal}
                    mainText={mainText}
                    okText="Ок"
                    onClose={this.handleCloseModal}
                />

                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Мои друзья"
                    user={this.context.user}
                    navigate={navigate}/>

                <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={this.context.user}/>

                <div className="friends-container">
                    <div className="friends-content">
                        <div className="friends-list-section">
                            <h2>Ваши друзья</h2>
                            <Pagination totalPages={totalFriendsPages} currentPage={currentFriendsPage}
                                        handlePageClick={this.handleFriendsPageClick}/>
                            {friends.length > 0 ? (
                                <ul className="friends-list">
                                    {friends.map(friend => (
                                        <li key={friend.id} className="friends-item">
                                            <span>{friend.displayName || friend.username}</span>
                                            <button className="remove-button"
                                                    onClick={() => this.handleRemoveFriend(friend.id)}>✖
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p className="no-friends">У вас пока нет друзей</p>
                            )}
                        </div>

                        <div className="search-section">
                            <h2>Поиск пользователей</h2>
                            <input
                                type="text"
                                className="search-input"
                                placeholder="Введите имя пользователя"
                                value={searchQuery}
                                onChange={this.handleInputChange}
                            />
                            <button className="search-button" onClick={() => this.handleSearch(1)}>Найти</button>

                            <div className="user-list">
                                <Pagination totalPages={totalUsersPages} currentPage={currentUsersPage}
                                            handlePageClick={this.handleUsersPageClick}/>
                                {searchResults.length === 0 ? (
                                    <p className="no-results-message">Пользователи не найдены</p>
                                ) : (
                                    searchResults.map(user => (
                                        <div key={user.id} className="search-result-item">
                                            <span>{user.displayName || user.username}</span>
                                            {sentRequests.some(req => req.id === user.id) ? (
                                                <span className="request-sent">Запрос отправлен</span>
                                            ) : user.isFriend ? (
                                                <button className="remove-button" onClick={() => this.handleRemoveFriend(user.id)}>✖</button>
                                            ) : incomingRequests.some(req => req.id === user.id) ? (
                                                <button className="accept-frineds-button" onClick={() => this.handleAcceptRequest(user.id)}>Принять</button>
                                            ) : (
                                                <button className="add-button" onClick={() => this.handleAddFriend(user.id)}>
                                                    Добавить в друзья
                                                </button>
                                            )}
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default withNavigation(FriendsPage);
