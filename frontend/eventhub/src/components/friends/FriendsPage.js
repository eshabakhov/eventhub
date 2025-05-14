import React, {Component} from 'react';
import '../../css/FriendsPage.css';
import {useNavigate} from "react-router-dom";
import API_BASE_URL from "../../config";
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import UserContext from "../../UserContext";
import ConfirmModal from "../common/ConfirmModal";

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
};

class FriendsPage extends Component {
    static contextType = UserContext;

    state = {
        friends: [],
        userId: null,
        searchQuery: '',
        searchResults: [],
        searchPerformed: false,
        incomingRequests: [],
        sentRequests: [],
        sidebarOpen: false
    };
    sidebarRef = React.createRef();

    componentDidMount() {
        document.addEventListener("mousedown", this.handleClickOutside);
        fetch(`${API_BASE_URL}/auth/me`, {
            credentials: 'include',
        })
            .then(res => res.json())
            .then(data => {
                const ctx = this.context;
                ctx.setUser(data.user);
                const userId = data.user.id;
                this.setState({userId});
                this.fetchFriends(userId);
            })
            .catch(console.error);
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

    fetchFriends = (id) => {
        fetch(`${API_BASE_URL}/v1/users/members/${id}/friends`, {
            credentials: 'include',
        })
            .then(res => res.json())
            .then(data => {
                const list = Array.isArray(data.list) ? data.list : [];
                const accepted = [];
                const incoming = [];
                const sent = [];

                list.forEach(item => {
                    const {sender, recipient, friendRequestStatus} = item;

                    if (friendRequestStatus === 'ACCEPTED') {
                        const friend = sender.id === id ? recipient : sender;
                        accepted.push(friend);
                    } else if (friendRequestStatus === 'PENDING') {
                        if (recipient.id === id) {
                            incoming.push(sender);
                        } else if (sender.id === id) {
                            sent.push(recipient);
                        }
                    }
                });

                this.setState({friends: accepted, incomingRequests: incoming, sentRequests: sent});
            })
            .catch(console.error);
    };

    handleSearch = () => {
        const {searchQuery} = this.state;

        if (!searchQuery.trim()) {
            this.setState({searchResults: [], searchPerformed: false});
            return;
        }

        fetch(`${API_BASE_URL}/v1/users?search=${encodeURIComponent(searchQuery)}`, {
            credentials: 'include',
        })
            .then(res => res.json())
            .then(data => {
                const results = Array.isArray(data.list) ? data.list : [];
                this.setState({searchResults: results, searchPerformed: true});
            })
            .catch(err => {
                console.error(err);
                this.setState({searchResults: [], searchPerformed: true});
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
        const {userId, searchResults} = this.state;

        fetch(`${API_BASE_URL}/v1/users/members/${userId}/friends/send/request?idTo=${recipientId}`, {
            method: 'POST',
            credentials: 'include',
            headers: {'Content-Type': 'application/json'}
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
        const {userId} = this.state;

        fetch(`${API_BASE_URL}/v1/users/members/${userId}/friends?idFrom=${friendId}`, {
            method: 'DELETE',
            credentials: 'include',
        })
            .then(() => this.fetchFriends(userId))
            .catch(console.error);
    };

    handleAcceptRequest = (senderId) => {
        const {userId} = this.state;

        fetch(`${API_BASE_URL}/v1/users/members/${userId}/friends/accept/request?idFrom=${senderId}`, {
            method: 'POST',
            credentials: 'include',
        })
            .then(() => this.fetchFriends(userId))
            .catch(console.error);
    };

    render() {
        const {navigate} = this.props;
        const {
            friends,
            searchQuery,
            searchResults,
            searchPerformed,
            incomingRequests,
            sentRequests,
            sidebarOpen,
            showConfirmModal,
            mainText
        } = this.state;
        console.log(this.context.user);

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
                                value={searchQuery}
                                onChange={this.handleInputChange}
                                placeholder="Поиск пользователей..."
                            />
                            <button className="search-button" onClick={this.handleSearch}>Найти</button>

                            {searchResults.length > 0 && (
                                <div className="search-results">
                                    <h3>Результаты поиска</h3>
                                    <ul>
                                        {searchResults.map(user => (
                                            <li key={user.id} className="search-result-item">
                                                <span>{user.displayName || user.username}</span>
                                                {sentRequests.some(req => req.id === user.id) ? (
                                                    <span className="request-sent">Запрос отправлен</span>
                                                ) : (
                                                    <button className="add-button"
                                                            onClick={() => this.handleAddFriend(user.id)}>Добавить в
                                                        друзья</button>
                                                )}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}

                            {searchPerformed && searchResults.length === 0 && (
                                <p className="no-results-message">Пользователь не найден</p>
                            )}

                            {incomingRequests.length > 0 && (
                                <div className="incoming-requests">
                                    <h3>Входящие запросы</h3>
                                    <ul>
                                        {incomingRequests.map(user => (
                                            <li key={user.id} className="incoming-request-item">
                                                <span>{user.displayName || user.username}</span>
                                                <button className="accept-frineds-button"
                                                        onClick={() => this.handleAcceptRequest(user.id)}>Принять
                                                </button>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}

                            {sentRequests.length > 0 && (
                                <div className="sent-requests">
                                    <h3>Отправленные запросы</h3>
                                    <ul>
                                        {sentRequests.map(user => (
                                            <li key={user.id} className="sent-request-item">
                                                {user.displayName || user.username}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default withNavigation(FriendsPage);
