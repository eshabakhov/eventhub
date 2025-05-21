import React, {Component} from "react";
import {useNavigate} from "react-router-dom";
import UserContext from "../../UserContext";
import CurrentUser from "../common/CurrentUser";
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import api from "../common/AxiosInstance";
import {motion} from "framer-motion";
import "../../css/Favorites.css";

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
    };

    sidebarRef = React.createRef();

    componentDidMount() {
        this.fetchOrganizers(1);
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

    render() {
        const {navigate} = this.props;

        const {
            favoriteOrganizers,
            sidebarOpen,} = this.state;

        return (
            <div className="events-container">
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Избранное"
                    user={this.context.user}
                    navigate={navigate}/>
                <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                <div className={`sidebar-overlay ${sidebarOpen ? 'active' : ''}`}></div>

                <SideBar sidebarOpen={sidebarOpen} sidebarRef={this.sidebarRef} user={this.context.user}/>
                <div className="favorites-container">
                    <div className="favorites-content">
                        <div className="favorites-list-section">
                            <h2>Избранные организаторы</h2>
                            {favoriteOrganizers.map((org) => (
                                <motion.div key={org.id} className="favorites-list-item" whileHover={{scale: 1.02}}>

                                    <div className="favorites-remove-container">
                                        <div className="star-container">
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
                                            <button
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
                        </div>
                    </div>
                </div>

            </div>
        );

    }

}

export default withNavigation(FavoritesPage);
