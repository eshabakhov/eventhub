import {useNavigate} from "react-router-dom";
import React, {Component} from "react";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";
import {motion} from "framer-motion";
import AcceptIcon from "../../img/accept.png";
import DeclineIcon from "../../img/decline.png";
import CrossIcon from "../../img/x.png";
import {MapContainer, Marker, Popup, TileLayer} from "react-leaflet";
import "../../css/Accreditation.css";
import UserContext from "../../UserContext";

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()} />;
}
class AccreditationPage extends Component {
    static contextType = UserContext;

    constructor(props) {
        super(props);
        this.markerRefs = React.createRef();
        this.markerRefs.current = {};
        this.state = {
            orgs: [],
            search: "",
            focusedOrg: null,
            currentPage: 1,
            orgsPerPage: 10,
            totalOrgs: 0,
            tags: [],
            selectedTags: [],
            showConfirmModal: false,
            selectedOrg: null,
        };
    }
    componentDidMount() {
        this.loadOrgs(1, this.state.search);
    }

    // Загрузка организаций
    loadOrgs = (page, search = "") => {
        const { orgsPerPage } = this.state;
        const searchParam = search ? `&search=${encodeURIComponent(search)}` : "";
        const currentUser = this.context.user
        let url=`http://localhost:9500/api/v1/users/organizers?${searchParam}&page=${page}&pageSize=${orgsPerPage}`;
        fetch(url, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
        })
            .then((res) => res.json())
            .then((data) => {
                const loadedOrgs = data.list.map((e) => ({
                    id: e.id,
                    name: e.name,
                    description: e.description,
                    industry: e.industry,
                    address: e.address,
                    isAccredited: e.isAccredited,
                }));
                this.setState({
                    orgs: loadedOrgs,
                    currentPage: page,
                    totalOrgs: data.total || 0,
                });
            })
            .catch((err) => console.error("Ошибка при загрузке организаций:", err));
    };

    // Обработка изменения поискового запроса
    handleSearchChange = (e) => {
        const newSearch = e.target.value;
        this.setState({ search: newSearch });
    };

    // Обработка перехода на другую страницу
    handlePageClick = (pageNumber) => {
        this.loadOrgs(pageNumber, this.state.search);
        const el = document.getElementsByClassName("content-panel")[0];
        el.scrollTo(0, 0);
    };

    render() {
        const { navigate } = this.props;
        const { orgs, search, currentPage, orgsPerPage, totalOrgs, showConfirmModal, selectedOrg } = this.state;
        const totalPages = Math.ceil(totalOrgs/ orgsPerPage);

        return (
            <div className="orgs-container">
                <div className="header">
                    <div className="top-logo" onClick={() => navigate("/events")} style={{ cursor: "pointer" }}>
                        <img src={EventHubLogo} alt="Logo" className="logo" />
                    </div>
                    <label className="panel-title">Аккредитация организаций</label>
                    <div className="login-button-container">
                        <ProfileDropdown navigate={navigate} />
                    </div>
                </div>
                <div className="content-panel">
                    <motion.div initial={{ opacity: 0, x: -50 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.5 }}>
                        {/* Поиск */}
                        <div className="search-wrapper">
                            <input
                                type="text"
                                placeholder="Поиск организаций"
                                className="search-input"
                                value={search}
                                onChange={this.handleSearchChange}
                                onKeyDown={(e) => {
                                    if (e.key === "Enter") this.loadOrgs(1, this.state.search);
                                }}
                            />
                            <button className="search-button-inside" onClick={() => this.loadOrgs(1, this.state.search)} aria-label="Поиск">
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35M11 19a8 8 0 100-16 8 8 0 000 16z" />
                                </svg>
                            </button>
                        </div>
                        {/* Верхняя пагинация */}
                        <div className={`pagination-controls ${totalPages < 2 ? "hidden" : ""}`}>
                            {Array.from({ length: totalPages }, (_, i) => (
                                <button key={i} className="pagination-button" disabled={currentPage === i + 1} onClick={() => this.handlePageClick(i + 1)}>
                                    {i + 1}
                                </button>
                            ))}
                        </div>

                        {/* Карточки событий */}
                        {orgs.map((org) => (
                            <motion.div key={org.id} className="event-card" whileHover={{ scale: 1.02 }}>

                                    <div className="params-buttons">
                                        <h3 className="org-title">{org.name}</h3>
                                        {org.isAccredited && (
                                            <button className="accept-button accr" title='Отменить акредитацию'
                                                    onClick={() => this.handleDeleteClick(org)}
                                            >
                                                {/*<img src={AcceptIcon} alt='Отменить акредитацию' className="icon"/>*/}
                                            </button>
                                        )}
                                        {!org.isAccredited && (
                                            <button className='accept-button not-accr' title='Акредитовать организацию'>
                                                {/*<img src={AcceptIcon} alt='Акредитовать организацию' className="icon"/>*/}
                                            </button>
                                        )}
                                    </div>


                                <span className="field">
                                    <div className="field-name"> Описание: </div>
                                    <div className>{org.description}</div>
                                </span>

                                <span className="field">
                                    <div className="field-name"> Сфера деятельности: </div>
                                    <div className>{org.industry}</div>
                                </span>

                                <span className="field">
                                    <div className="field-name"> Адрес: </div>
                                     <div className>{org.address}</div>
                                </span>

                                <div className="card-buttons">
                                    {/*<div className="button-group">*/}
                                    {/*    <button onClick={() => navigate(`/event/${org.id}`)} className="event-button details">*/}
                                    {/*        Подробнее*/}
                                    {/*    </button>*/}
                                    {/*    <button*/}
                                    {/*        // onClick={() =>*/}
                                    {/*        //     this.setState({*/}
                                    {/*        //         focusedEvent: event,*/}
                                    {/*        //         focusedMarkerId: this.getMarkerIdForEvent(event),*/}
                                    {/*        //     })*/}
                                    {/*        // }*/}
                                    {/*        className="event-button map"*/}
                                    {/*    >*/}
                                    {/*        Показать на карте*/}
                                    {/*    </button>*/}
                                    {/*</div>*/}
                                    <div className={`org-status ${org.isAccredited}`}>
                                        {org.isAccredited ? "Организация акредитована" : "Организация не аккредитована"}</div>
                                </div>
                            </motion.div>
                        ))}
                        {/* Нижняя пагинация */}
                        <div className={`pagination-controls ${totalPages < 2 ? "hidden" : ""}`}>
                            {Array.from({ length: totalPages }, (_, i) => (
                                <button key={i} className="pagination-button" disabled={currentPage === i + 1} onClick={() => this.handlePageClick(i + 1)}>
                                    {i + 1}
                                </button>
                            ))}
                        </div>
                    </motion.div>
                </div>
            </div>

        );
    }
}
export default withNavigation(AccreditationPage);