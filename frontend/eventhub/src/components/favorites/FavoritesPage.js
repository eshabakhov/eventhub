import React, {Component} from "react";
import {useNavigate} from "react-router-dom";
import UserContext from "../../UserContext";
import CurrentUser from "../common/CurrentUser";
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import api from "../common/AxiosInstance";
import {motion} from "framer-motion";
import "../../css/Favorites.css";
import ConfirmModal from "../common/ConfirmModal";
import API_BASE_URL from "../../config";

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
};

class FavoritesPage extends Component {
    static contextType = UserContext;

    state = {
        favoriteOrganizers: [],
        sidebarOpen: false,
        currentOrgPage: 1,
        orgsPerPage: 10,
        totalOrgs: 0,
        showConfirmModal: false,
        selectedOrg: null,
        selectedTag: null,
        tags: [],
    };

    sidebarRef = React.createRef();

    componentDidMount() {
        this.fetchOrganizers(1);
        this.loadFavoriteTags();
        document.addEventListener("mousedown", this.handleClickOutside);
        CurrentUser.fetchCurrentUser(this.context.setUser);
    }

    componentWillUnmount() {
        document.removeEventListener("mousedown", this.handleClickOutside);
    }

    toggleSidebar = () => {
        this.setState(prev => ({sidebarOpen: !prev.sidebarOpen}));
    };

    fetchOrganizers = (page) => {
        const {orgsPerPage} = this.state;
        const member = this.context.user;
        api.get(`/v1/members/${member.id}/favorite-organizers?page=${page}&pageSize=${orgsPerPage}`, {
            credentials: 'include',
        })
            .then(res => res.data)
            .then(data => {
                this.setState({
                    favoriteOrganizers: data.list || [],
                    totalOrgs: data.total,
                    currentOrgPage: page});
                console.log(data.list);
            })
            .catch(console.error);
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

    handleRemoveFavorite = () => {
        const {selectedOrg, selectedTag} = this.state;
        const {user} = this.context;
        if (!selectedOrg && !selectedTag || !user) {
            this.handleCloseModal();
            return;
        }

        if (selectedTag) {
            this.removeFavoriteTag(selectedTag, user);
            return;
        }
        if (selectedOrg) {
            this.removeOrgFavorite(selectedOrg, user);
        }
    };
    removeFavoriteTag = async (selectedTag, member) => {
        try {
            await api.delete(`/v1/tags/${selectedTag.id}/users/${member.id}`);
            this.loadFavoriteTags();
        } catch (err) {
            console.error("Ошибка при отмене аккредитации", err);
        } finally {
            this.handleCloseModal();
        }
    };

    removeOrgFavorite = async (selectedOrg, member) => {
        try {
            await api.delete(`/v1/members/${member.id}/organizers/${selectedOrg.id}`);
            this.fetchOrganizers(1);
        } catch (err) {
            console.error("Ошибка при отмене аккредитации", err);
        } finally {
            this.handleCloseModal();
        }
    };

    // Открытие модального окна
    handleOrgStarClick = (org) => {
        this.setState({
            showConfirmModal: true,
            selectedOrg: org
        });
    };

    handleTagStarClick = (tag) => {
        this.setState({
            showConfirmModal: true,
            selectedTag: tag
        });
    };

    // Закрытие модального окна
    handleCloseModal = () => {
        this.setState({
            showConfirmModal: false,
            selectedOrg: null,
            selectedTag: null
        });
    };

    // Загрузка избранных тегов
    loadFavoriteTags = () => {
        const currentUser = this.context.user;
        if (!currentUser || !currentUser.id) return;

        fetch(`${API_BASE_URL}/v1/tags/${currentUser.id}`, {
            method: "GET",
            headers: {"Content-Type": "application/json"},
            credentials: "include",
        })
            .then((res) => res.json())
            .then((data) => {
                this.setState({tags : data.list})
                })
            .catch((err) => console.error("Ошибка при загрузке избранных тегов:", err));
    };

    render() {
        const {navigate} = this.props;
        const {
            favoriteOrganizers,
            sidebarOpen,
            selectedOrg, showConfirmModal, tags, selectedTag} = this.state;

        return (
            <div className="events-container">
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Избранное"
                    user={this.context.user}
                    navigate={navigate}/>
                <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={this.context.user}/>

                <ConfirmModal
                    headerText="Подтверждение"
                    mainText={selectedOrg && `Удалить организацию "${selectedOrg.name}" из избранного?` || selectedTag && `Удалить тег "${selectedTag.name}" из избранного?`}
                    cancelText="Отмена"
                    confirmText="Подтвердить"
                    isOpen={showConfirmModal}
                    onClose={this.handleCloseModal}
                    onConfirm={this.handleRemoveFavorite}
                />

                <div className="favorites-container">
                    <div className="favorites-content">
                        <div className="favorites-list-section">
                            <h2>Избранные организаторы</h2>
                            {favoriteOrganizers.map((org) => (
                                <motion.div key={org.id} className="favorites-list-item" whileHover={{scale: 1.02}}>

                                    <div className="favorites-remove-container">
                                        <div className="star-container" onClick={() => this.handleOrgStarClick(org)}>
                                            <svg
                                                className={`favorites-remove-button`}
                                                viewBox="0 0 24 24"
                                            >
                                                <path
                                                    d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                                                />
                                            </svg>
                                        </div>
                                    </div>

                                    <div className="favorites-list-item-header">
                                        <h3 className="org-title">{org.name}</h3>
                                        <div className={`org-status ${org.isAccredited}`}>
                                            {org.isAccredited ? "Организация аккредитована" : "Организация не аккредитована"}</div>
                                    </div>

                                    <span className="list-item-field">
                                        <div className="field-name"> Описание: </div>
                                        <div className>{org.description}</div>
                                    </span>

                                    <span className="list-item-field">
                                        <div className="field-name"> Сфера деятельности: </div>
                                        <div className>{org.industry}</div>
                                    </span>

                                    <span className="list-item-field">
                                        <div className="field-name"> Адрес: </div>
                                        <div className>{org.address}</div>
                                    </span>
                                    <div className="card-buttons">
                                        <div className="button-group">
                                            <button onClick={() => navigate(`/organizers/${org.id}`)}
                                                    className="event-button details">
                                                Подробнее
                                            </button>
                                        </div>
                                    </div>
                                </motion.div>
                            ))}
                        </div>

                        <div className="favorites-list-section">
                            <h2>Избранные теги</h2>
                            {/* Фильтр по тегам */}
                            <div className="tags-filter-wrapper">
                                {tags.map((tag) => {
                                    return (
                                        <button
                                            key={tag.name}
                                            onClick={() => this.handleTagStarClick(tag)}
                                            className={`tag-favorites-button`}
                                        >
                                            {tag.name}
                                            <div className="star-container">
                                                <svg
                                                    className={`tag-favorites-star`}
                                                    viewBox="0 0 24 24"
                                                >
                                                    <path
                                                        d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                                                    />
                                                </svg>
                                            </div>
                                        </button>
                                    );
                                })}
                            </div>

                        </div>
                    </div>
                </div>

            </div>
        );

    }

}

export default withNavigation(FavoritesPage);
