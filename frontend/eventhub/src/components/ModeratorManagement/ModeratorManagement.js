import React, {Component} from "react";
import {motion} from "framer-motion";
import EditIcon from "../../img/edit.png";
import DeleteIcon from "../../img/delete.png";
import "../../css/ModeratorManagement.css";
import {useNavigate} from "react-router-dom";
import UserContext from "../../UserContext";
import Header from "../common/Header";
import SideBar from "../common/SideBar";
import ConfirmModal from "../common/ConfirmModal";
import api from "../common/AxiosInstance";

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
}

class ModeratorsPage extends Component {
    static contextType = UserContext;

    constructor(props) {
        super(props);
        this.state = {
            showConfirmModal: false,
            moderators: [],
            selectedModer: null,
        };
    }

    sidebarRef = React.createRef();

    componentDidMount() {
        this.loadModerators();
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

    loadModerators = async () => {
        try {
            const response = await api.get(`/v1/users/moderators`);
            const data = response.data;

            const loadedModerators = data.list.map((e) => ({
                id: e.id,
                displayName: e.displayName,
                email: e.email,
                userName: e.username
            }))

            this.setState({moderators: loadedModerators});
        }  catch (err) {
            console.error("Ошибка загрузки модераторов:", err);
        }
    };

    handleEdit = (moderatorId) => {
        // TODO: переход к форме редактирования
        this.props.navigate(`/moderators/edit/${moderatorId}`);
    };

    // Открытие модального окна изменения аккредитации
    handleChangeModer = (mod) => {
        this.setState({
            showConfirmModal: true,
            selectedModer: mod
        });
    };

    handleDelete = async (moderatorId) => {
        try {
            const response = await api.delete(`/v1/users/${moderatorId}`);
            if (response.status === 204) {
                await this.loadModerators();
            } else {
                alert("Ошибка удаления модератора");
            }
        } catch (error) {
            console.error("Ошибка удаления модератора:", error)
        } finally {
            this.handleCloseModal();
        }
    };

    handleCreate = () => {
        this.props.navigate("/moderators/create");
    };

    // Подтверждение
    handleConfirm = () => {
        const {selectedModer} = this.state;
        const {user} = this.context;
        if (!selectedModer || !user) {
            this.handleCloseModal();
            return;
        }
        this.handleDelete(selectedModer.id);

    };

    handleCloseModal = () => {
        this.setState({
            showConfirmModal: false,
            selectedModer: null
        });
    };

    render() {
        const {moderators, showConfirmModal, selectedModer} = this.state;
        const {navigate} = this.props;


        return (
            <div className="orgs-container">
                <ConfirmModal
                    isOpen={showConfirmModal}
                    headerText="Подтверждение"
                    mainText={`Удалить модератора "${selectedModer && selectedModer.displayName}"?`}
                    confirmText="Удалить"
                    cancelText="Отмена"
                    onClose={this.handleCloseModal}
                    onConfirm={this.handleConfirm}
                />
                <Header
                    onBurgerButtonClick={this.toggleSidebar}
                    title="Управление модераторами"
                    user={this.context.user}
                    navigate={navigate}
                />

                <div className={`sidebar-overlay ${this.state.sidebarOpen ? 'active' : ''}`}></div>

                <SideBar user={this.context.user} sidebarRef={this.sidebarRef} sidebarOpen={this.state.sidebarOpen}/>

                <div className="content-panel">
                    <motion.div initial={{opacity: 0, x: -50}} animate={{opacity: 1, x: 0}}
                                transition={{duration: 0.5}}>
                        <div className="button-group" style={{marginBottom: "20px"}}>
                            <button onClick={this.handleCreate} className="event-button details">Создать модератора
                            </button>
                        </div>

                        {moderators.map((mod) => (
                            <motion.div key={mod.id} className="moderator-card" whileHover={{scale: 1.02}}>
                                <div className="buttons">
                                    <h3 className="org-title">{mod.displayName || mod.username}</h3>
                                    <div>
                                        <button className="edit-button" title="Редактировать"
                                                onClick={() => this.handleEdit(mod.id)}>
                                            <img src={EditIcon} alt='Редактировать' className="icon"/>
                                        </button>
                                        <button className="delete-button" title="Удалить"
                                                onClick={() => this.handleChangeModer(mod)}>
                                            <img src={DeleteIcon} alt='Удалить' className="icon"/>
                                        </button>
                                    </div>
                                </div>

                                <span className="field">
                                    <div className="field-name">Логин:</div>
                                    <div>{mod.userName}</div>
                                </span>

                                <span className="field">
                                    <div className="field-name">Email:</div>
                                    <div>{mod.email}</div>
                                </span>
                            </motion.div>
                        ))}
                    </motion.div>
                </div>
            </div>
        );
    }
}

export default withNavigation(ModeratorsPage);
