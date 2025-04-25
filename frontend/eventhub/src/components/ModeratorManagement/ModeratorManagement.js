import React, {Component} from "react";
import {motion} from "framer-motion";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";
import EditIcon from "../../img/edit.png";
import DeleteIcon from "../../img/delete.png";
import "../../css/Accreditation.css";
import {useNavigate} from "react-router-dom";
import UserContext from "../../UserContext";
import API_BASE_URL from "../../config";

export const withNavigation = (WrappedComponent) => {
    return (props) => <WrappedComponent {...props} navigate={useNavigate()}/>;
}

const ConfirmModal = ({isOpen, onClose, onConfirm, mod}) => {
    if (!isOpen) return null;
    return (
        <div className="modal-overlay">
            <motion.div
                className="modal-content"
                initial={{scale: 0.9, opacity: 0}}
                animate={{scale: 1, opacity: 1}}
                exit={{scale: 0.9, opacity: 0}}
            >
                <h3>Подтверждение</h3>
                <p>Удалить модератора "{mod.displayName}"?</p>
                <div className="modal-buttons">
                    <button className="modal-button cancel" onClick={onClose}>
                        Отмена
                    </button>
                    <button className="modal-button confirm" onClick={onConfirm}>
                        Удалить
                    </button>
                </div>
            </motion.div>
        </div>
    );
};

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

    componentDidMount() {
        this.loadModerators();
    }

    loadModerators = () => {
        fetch(`${API_BASE_URL}/users/moderators`, {
            method: "GET",
            headers: {"Content-Type": "application/json"},
            credentials: "include",
        })
            .then(res => res.json())
            .then(data => {
                const loadedModerators = data.list.map((e) => ({
                    id: e.id,
                    displayName: e.displayName,
                    email: e.email,
                    userName: e.username
                }))
                this.setState({moderators: loadedModerators});
            })
            .catch(err => console.error("Ошибка загрузки модераторов:", err));
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

    handleDelete = (moderatorId) => {
        fetch(`${API_BASE_URL}/users/${moderatorId}`, {
            method: "DELETE",
            credentials: "include",
        })
            .then(res => {
                if (res.ok) this.loadModerators();
                else alert("Ошибка удаления модератора");
            })
            .catch(err => console.error("Ошибка удаления модератора:", err))
            .finally(()=>{
                this.handleCloseModal();
            });
    };

    handleCreate = () => {
        this.props.navigate("/moderators/create");
    };

    // Подтверждение
    handleConfirm = () => {
        const { selectedModer } = this.state;
        const { user } = this.context;
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
                    onClose={this.handleCloseModal}
                    onConfirm={this.handleConfirm}
                    mod={selectedModer}
                    user={this.context.user}
                />
                <div className="header">
                    <div className="top-logo" onClick={() => navigate("/events")} style={{cursor: "pointer"}}>
                        <img src={EventHubLogo} alt="Logo" className="logo"/>
                    </div>
                    <h1 className="panel-title">Управление модераторами</h1>
                    <div className="login-button-container">
                        <ProfileDropdown navigate={navigate}/>
                    </div>
                </div>

                <div className="content-panel">
                    <motion.div initial={{opacity: 0, x: -50}} animate={{opacity: 1, x: 0}}
                                transition={{duration: 0.5}}>
                        <div className="button-group" style={{marginBottom: "20px"}}>
                            <button onClick={this.handleCreate} className="event-button details">Создать модератора
                            </button>
                        </div>

                        {moderators.map((mod) => (
                            <motion.div key={mod.id} className="event-card" whileHover={{scale: 1.02}}>
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
