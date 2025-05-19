import {useNavigate} from "react-router-dom";
import React, {Component} from "react";
import {motion} from "framer-motion";
import "../../css/Accreditation.css";
import UserContext from "../../UserContext";
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import Pagination from "../common/Pagination";
import ConfirmModal from "../common/ConfirmModal";
import api from '../common/AxiosInstance';

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
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

    sidebarRef = React.createRef();

    componentDidMount() {
        this.loadOrgs(1, this.state.search);
        document.addEventListener("mousedown", this.handleClickOutside);
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

    // Загрузка организаций
    loadOrgs = async (page, search = "") => {
        const {orgsPerPage} = this.state;
        const searchParam = search ? `&search=${encodeURIComponent(search)}` : "";

        try {
            const response = await api.get(`/v1/users/organizers?page=${page}&pageSize=${orgsPerPage}${searchParam}`);
            const data = response.data;

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
        } catch (err) {
            console.error("Ошибка при загрузке организаций:", err);
        }
    };

    // Обработка изменения поискового запроса
    handleSearchChange = (e) => {
        const newSearch = e.target.value;
        this.setState({search: newSearch});
    };

    // Закрытие модального окна
    handleCloseModal = () => {
        this.setState({
            showConfirmModal: false,
            selectedOrg: null
        });
    };

    // Подтверждение
    handleConfirm = () => {
        const {selectedOrg} = this.state;
        const {user} = this.context;
        if (!selectedOrg || !user) {
            this.handleCloseModal();
            return;
        }

        if (selectedOrg.isAccredited) {
            this.cancelAccreditation(selectedOrg, user);
        }
        if (!selectedOrg.isAccredited) {
            this.approveAccreditation(selectedOrg, user);
        }
    };

    cancelAccreditation = async (selectedOrg) => {
        try {
            await api.put(`/v1/users/organizers/${selectedOrg.id}`, {
                isAccredited: "false",
            });
            this.loadOrgs(this.state.currentPage, this.state.search);
        } catch (err) {
            console.error("Ошибка при отмене аккредитации", err);
        } finally {
            this.handleCloseModal();
        }
    };

    approveAccreditation = async (selectedOrg) => {
        try {
            await api.put(`/v1/users/organizers/${selectedOrg.id}`, {
                isAccredited: "true",
            });
            this.loadOrgs(this.state.currentPage, this.state.search);
        } catch (err) {
            console.error("Ошибка при добавлении аккредитации", err);
        } finally {
            this.handleCloseModal();
        }
    };

    // Обработка перехода на другую страницу
    handlePageClick = (pageNumber) => {
        this.loadOrgs(pageNumber, this.state.search);
        const el = document.getElementsByClassName("content-panel")[0];
        el.scrollTo(0, 0);
    };

    // Открытие модального окна изменения аккредитации
    handleChangeAccrClick = (org) => {
        this.setState({
            showConfirmModal: true,
            selectedOrg: org
        });
    };

    render() {
        const {navigate} = this.props;
        const {orgs, search, currentPage, orgsPerPage, totalOrgs, showConfirmModal, selectedOrg} = this.state;
        const totalPages = Math.ceil(totalOrgs / orgsPerPage);

        return (
            <div className="orgs-container">
                {/* Модальное окно подтверждения */}
                <ConfirmModal
                    headerText="Подтверждение"
                    mainText={selectedOrg && (selectedOrg.isAccredited ? `Отменить аккредитацию организации "${selectedOrg.name}"?` : `Аккредитовать организацию "${selectedOrg.name}"?`)}
                    cancelText="Отмена"
                    confirmText="Подтвердить"
                    isOpen={showConfirmModal}
                    onClose={this.handleCloseModal}
                    onConfirm={this.handleConfirm}
                />
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Аккредитация организаций"
                    user={this.context.user}
                    navigate={navigate}
                />
                <div className={`sidebar-overlay ${this.state.sidebarOpen ? 'active' : ''}`}></div>

                <SideBar user={this.context.user} sidebarRef={this.sidebarRef} sidebarOpen={this.state.sidebarOpen}/>

                <div className="content-panel">
                    <motion.div initial={{opacity: 0, x: -50}} animate={{opacity: 1, x: 0}}
                                transition={{duration: 0.5}}>
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
                            <button className="search-button-inside" onClick={() => this.loadOrgs(1, this.state.search)}
                                    aria-label="Поиск">
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none"
                                     viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M21 21l-4.35-4.35M11 19a8 8 0 100-16 8 8 0 000 16z"/>
                                </svg>
                            </button>
                        </div>
                        {/* Верхняя пагинация */}
                        <Pagination totalPages={totalPages} currentPage={currentPage}
                                    handlePageClick={this.handlePageClick}/>

                        {/* Карточки событий */}
                        {orgs.map((org) => (
                            <motion.div key={org.id} className="org-card" whileHover={{scale: 1.02}}>

                                <div className="buttons">
                                    <h3 className="org-title">{org.name}</h3>
                                    {org.isAccredited && (
                                        <button className="accept-button accr" title='Отменить аккредитацию'
                                                onClick={() => this.handleChangeAccrClick(org)}
                                        >
                                        </button>
                                    )}
                                    {!org.isAccredited && (
                                        <button className='accept-button not-accr' title='Аккредитовать организацию'
                                                onClick={() => this.handleChangeAccrClick(org)}>
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
                                    <div className={`org-status ${org.isAccredited}`}>
                                        {org.isAccredited ? "Организация аккредитована" : "Организация не аккредитована"}</div>
                                </div>
                            </motion.div>
                        ))}
                        {/* Нижняя пагинация */}
                        <Pagination totalPages={totalPages} currentPage={currentPage}
                                    handlePageClick={this.handlePageClick}/>
                    </motion.div>
                </div>
            </div>
        );
    }
}

export default withNavigation(AccreditationPage);