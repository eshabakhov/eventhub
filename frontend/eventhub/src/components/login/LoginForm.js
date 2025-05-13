import React from "react"
import "../../css/AuthPage.css";
import UserContext from "../../UserContext";
import {Navigate} from "react-router";
import API_BASE_URL from "../../config";
import EventHubLogo from "../../img/eventhub.png";
import {useNavigate, useLocation} from "react-router-dom";
import ConfirmModal from "../common/ConfirmModal";

export const withNavigation = (WrappedComponent) => {
    return (props) => {
        const navigate = useNavigate();
        const location = useLocation();
        return <WrappedComponent {...props} navigate={navigate} location={location}/>;
    };
};

class Login extends React.Component {
    static contextType = UserContext;

    constructor(props) {
        super(props);
        this.state = {
            isLogin: true,
            step: 1,
            role: '',
            username: '',
            email: '',
            password: '',
            confirmPassword: '',

            // Дополнительные поля
            // Организатор
            orgName: '',
            shortDesc: '',
            fullDesc: '',
            industry: '',
            address: '',

            // Участник
            lastName: '',
            firstName: '',
            middleName: '',
            birthDate: '',
            birthCity: '',
            privacy: 'PRIVATE',

            redirect: false,
            from: props.location?.state?.from || null
        };
    }

    handleToggle = () => {
        this.setState({
            isLogin: !this.state.isLogin,
            step: 1,
            username: '',
            email: '',
            password: '',
            confirmPassword: '',
            role: '',
        });
    };

    handleChange = e => {
        this.setState({[e.target.name]: e.target.value});
    };

    handleNext = e => {
        e.preventDefault();
        const {username, email, password, confirmPassword, role} = this.state;
        if (!username || !email || !password || password !== confirmPassword || !role) {
            this.setState({
                showConfirmModal: true,
                mainText: "Пожалуйста, заполните все поля корректно"
            });
            return;
        }
        this.setState({step: 2});
    };

    handleSubmit = (e) => {
        e.preventDefault();

        const {
            isLogin,
            step,
            username,
            password,
            confirmPassword,
            role,
            email,
            displayName,
            orgName,
            shortDesc,
            fullDesc,
            industry,
            address,
            lastName,
            firstName,
            middleName,
            birthDate,
            birthCity,
            privacy
        } = this.state;

        if (isLogin) {
            // Обработка входа
            if (!username || !password) {
                this.setState({
                    showConfirmModal: true,
                    mainText: "Введите имя пользователя и пароль"
                });
                return;
            }

            fetch(`${API_BASE_URL}/auth/login`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                credentials: "include",
                body: JSON.stringify({username, password})
            })
                .then(res => {
                    if (!res.ok) throw new Error("Неверные учетные данные");
                })
                .then(data => {
                    this.setState({redirect: true});
                })
                .catch(err => {
                    console.error(err);
                    this.setState({
                        showConfirmModal: true,
                        mainText: "Ошибка при входе"
                    });
                });

        } else if (step === 2) {
            // Обработка регистрации
            let roleValid = true;

            if (role === "ORGANIZER") {
                roleValid = orgName && shortDesc && fullDesc && industry && address;
            } else if (role === "MEMBER") {
                roleValid = lastName && firstName && middleName && birthDate && birthCity && privacy;
            }

            if (!roleValid) {
                this.setState({
                    showConfirmModal: true,
                    mainText: "Пожалуйста, заполните все поля на втором шаге"
                });
                return;
            }

            const commonData = {
                username,
                email,
                password,
                displayName,
                role
            };

            // 1. Создание пользователя
            fetch(`${API_BASE_URL}/v1/users`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                credentials: "include",
                body: JSON.stringify(commonData)
            })
                .then(res => {
                    if (!res.ok) throw new Error("Ошибка при создании пользователя");
                    return res.json();
                })
                .then(user => {
                    const userId = user.id;

                    // 2. Авторизация нового пользователя
                    return fetch(`${API_BASE_URL}/auth/login`, {
                        method: "POST",
                        headers: {"Content-Type": "application/json"},
                        credentials: "include",
                        body: JSON.stringify({username, password})
                    }).then(res => {
                        if (!res.ok) throw new Error("Ошибка при входе нового пользователя");
                        return {userId};
                    });
                })
                .then(({userId}) => {
                    // 3. Отправка данных роли
                    let url = "";
                    let rolePayload = {};

                    if (role === "ORGANIZER") {
                        url = `${API_BASE_URL}/v1/users/organizers/${userId}`;
                        rolePayload = {
                            name: orgName,
                            description: fullDesc,
                            shortDescription: shortDesc,
                            industry,
                            address
                        };
                    } else {
                        url = `${API_BASE_URL}/v1/users/members/${userId}`;
                        rolePayload = {
                            lastName,
                            firstName,
                            patronymic: middleName,
                            birthDate,
                            birthCity,
                            privacy
                        };
                    }

                    return fetch(url, {
                        method: "PUT",
                        headers: {"Content-Type": "application/json"},
                        credentials: "include",
                        body: JSON.stringify(rolePayload)
                    });
                })
                .then(res => {
                    if (!res.ok) throw new Error("Ошибка при обновлении данных роли");
                    this.setState({redirect: true});
                })
                .catch(err => {
                    console.error(err);
                    this.setState({
                        showConfirmModal: true,
                        mainText: "Ошибка регистрации"
                    });
                });
        }
    };

    handleRegistration = () => {
        const {
            username, email, password, role,
            orgName, shortDesc, fullDesc, industry, address,
            lastName, firstName, middleName, birthDate, birthCity, privacy
        } = this.state;

        const commonData = {username, email, password, role};

        let roleData = {};
        if (role === 'organizer') {
            roleData = {orgName, shortDesc, fullDesc, industry, address};
        } else {
            roleData = {lastName, firstName, middleName, birthDate, birthCity, privacy};
        }

        const payload = {...commonData, ...roleData};

        fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload),
            credentials: 'include'
        })
            .then(res => res.json())
            .then(data => {
                this.setState({
                    showConfirmModal: true,
                    mainText: "Регистрация успешна!"
                });
                this.setState({isLogin: true, step: 1}); // Возврат к логину
            })
            .catch(err => {
                console.error(err);
                this.setState({
                    showConfirmModal: true,
                    mainText: "Ошибка регистрации"
                });
            });
    };

    handleGoogleLogin = () => {
        window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
    };

    handleYandexLogin = () => {
        window.location.href = `${API_BASE_URL}/oauth2/authorization/yandex`;
    };

    renderLogin() {
        const {username, password, showConfirmModal, mainText} = this.state;
        const {navigate} = this.props;
        return (
            <>
                <ConfirmModal
                    isOpen={showConfirmModal}
                    mainText={mainText}
                    okText="Ок"
                    onClose={this.handleCloseModal}
                />
                <svg width="250" height="60" xmlns="http://www.w3.org/2000/svg" onClick={() => navigate("/events")} style={{cursor: "pointer"}}>
                <defs>
                    <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stopColor="#1774c5" stopOpacity="1" />
                    <stop offset="100%" stopColor="#4dc3ff" stopOpacity="1" />
                    </linearGradient>
                </defs>
                <text
                    x="50%"
                    y="50%"
                    fontFamily="Segoe UI, sans-serif"
                    fontSize="30"
                    fontWeight="bold"
                    fill="url(#grad1)"
                    textAnchor="middle"
                    dominantBaseline="middle"
                >
                    eventhub
                </text>
                </svg>
                {/* <div className="top-logo" onClick={() => navigate("/events")} style={{cursor: "pointer"}}>
                    <img src={EventHubLogo} alt="Logo" className="logo"/>
                </div>
                <h2>Вход</h2> */}
                <form onSubmit={this.handleSubmit}>
                    <input
                        type="text"
                        name="username"
                        placeholder="Имя пользователя"
                        value={username}
                        onChange={this.handleChange}
                        required
                    />
                    <input
                        type="password"
                        name="password"
                        placeholder="Пароль"
                        value={password}
                        onChange={this.handleChange}
                        required
                    />
                    <button className="auth-button" type="submit">Войти</button>
                    <div className="custom-auth">
                        <a class="login-with-google-btn" onClick={this.handleGoogleLogin}>
                        <span class="google-icon"></span>
                            Войти через Google
                        </a>
                        {/* <button type="submit-google" class="login-with-google-btn" onClick={this.handleGoogleLogin} >
                            Войти через Google
                        </button> */}
                        {/* <button id="VKIDSDKAuthButton" class="VkIdWebSdk__button VkIdWebSdk__button_reset">
                            <div class="VkIdWebSdk__button_container">
                                <div class="VkIdWebSdk__button_icon">
                                    <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path fill-rule="evenodd" clip-rule="evenodd" d="M4.54648 4.54648C3 6.09295 3 8.58197 3 13.56V14.44C3 19.418 3 21.907 4.54648 23.4535C6.09295 25 8.58197 25 13.56 25H14.44C19.418 25 21.907 25 23.4535 23.4535C25 21.907 25
                                    19.418 25 14.44V13.56C25 8.58197 25 6.09295 23.4535 4.54648C21.907 3 19.418 3 14.44 3H13.56C8.58197 3 6.09295 3 4.54648 4.54648ZM6.79999 10.15C6.91798 15.8728 9.92951 19.31 14.8932 19.31H15.1812V16.05C16.989 16.2332 18.3371
                                    17.5682 18.8875 19.31H21.4939C20.7869 16.7044 18.9535 15.2604 17.8141 14.71C18.9526 14.0293 20.5641 12.3893 20.9436 10.15H18.5722C18.0747 11.971 16.5945 13.6233 15.1803 13.78V10.15H12.7711V16.5C11.305 16.1337 9.39237 14.3538 9.314 10.15H6.79999Z" fill="white"/>
                                    </svg>
                                </div>
                                <div class="VkIdWebSdk__button_text">
                                    Войти с VK ID
                                </div>
                            </div>
                        </button> */}
                        <a class="yandex-oauth-btn" onClick={this.handleYandexLogin}>
                        <span class="yandex-icon"></span>
                            Войти с Яндекс ID
                        </a>
                    </div>
                </form>
                <p onClick={this.handleToggle} className="toggle-link">
                    Нет аккаунта? Зарегистрироваться
                </p>
            </>
        );
    }

    handleNext = (e) => {
        e.preventDefault();
        const {username, email, password, confirmPassword, role, displayName, showConfirmModal} = this.state;
        console.log(username, email, password, confirmPassword, role, displayName);
        if (!username || !email || !password || password !== confirmPassword || !role || !displayName) {
            this.setState({
                showConfirmModal: true,
                mainText: "Пожалуйста, заполните все поля корректно"
            });
            return;
        }

        this.setState({step: 2});
    };

    handleCloseModal = () => {
        this.setState({
            showConfirmModal: false,
            mainText: ""
        });
    };

    renderStep1() {
        const {username, password, confirmPassword, email, role, displayName, showConfirmModal, mainText} = this.state;

        return (
            <>
                <ConfirmModal
                    isOpen={showConfirmModal}
                    mainText={mainText}
                    okText="Ок"
                    onClose={this.handleCloseModal}
                />

                <h2>Регистрация</h2>
                <form onSubmit={this.handleNext}>
                    <select name="role" value={role} onChange={this.handleChange} required>
                        <option value="">Выберите роль</option>
                        <option value="ORGANIZER">Организатор</option>
                        <option value="MEMBER">Участник</option>
                    </select>
                    <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={email}
                        onChange={this.handleChange}
                        required
                    />
                    <input
                        type="text"
                        name="username"
                        placeholder="Логин"
                        value={username}
                        onChange={this.handleChange}
                        required
                    />
                    <input
                        type="password"
                        name="password"
                        placeholder="Пароль"
                        value={password}
                        onChange={this.handleChange}
                        required
                    />
                    <input
                        type="password"
                        name="confirmPassword"
                        placeholder="Повторите пароль"
                        value={confirmPassword}
                        onChange={this.handleChange}
                        required
                    />
                    <input
                        type="text"
                        name="displayName"
                        placeholder="Отображаемое имя пользователя"
                        value={displayName}
                        onChange={this.handleChange}
                        required
                    />
                    <button className="auth-button" type="submit">Далее</button>
                </form>
                <p onClick={this.handleToggle} className="toggle-link">
                    Уже есть аккаунт? Войти
                </p>
            </>
        );
    }


    renderStep2() {
        const {role, showConfirmModal, mainText} = this.state;
        return (
            <>
                <ConfirmModal
                    isOpen={showConfirmModal}
                    mainText={mainText}
                    okText="Ок"
                    onClose={this.handleCloseModal}
                />

                <h2>Регистрация</h2>
                <form onSubmit={this.handleSubmit}>
                    {role === 'ORGANIZER' ? (
                        <>
                            <input name="orgName" placeholder="Название организации" onChange={this.handleChange} required />
                            <input name="shortDesc" placeholder="Краткое описание" onChange={this.handleChange} required />
                            <textarea name="fullDesc" placeholder="Полное описание" onChange={this.handleChange} required />
                            <input name="industry" placeholder="Сфера деятельности" onChange={this.handleChange} required />
                            <input name="address" placeholder="Адрес" onChange={this.handleChange} required/>
                        </>
                    ) : (
                        <>
                            <input name="lastName" placeholder="Фамилия" onChange={this.handleChange} required />
                            <input name="firstName" placeholder="Имя" onChange={this.handleChange} required />
                            <input name="middleName" placeholder="Отчество" onChange={this.handleChange} required />
                            <input type="date" name="birthDate" onChange={this.handleChange} required />
                            <input name="birthCity" placeholder="Город рождения" onChange={this.handleChange} required />
                            <select name="privacy" onChange={this.handleChange}>
                                <option value="PRIVATE">Приватный</option>
                                <option value="PUBLIC">Публичный</option>
                                <option value="ONLY_FRIENDS">Только для друзей</option>
                            </select>
                        </>
                    )}
                    <button className="auth-button" type="submit">Зарегистрироваться</button>
                </form>
            </>
        );
    }


    render() {
        const {isLogin, step, redirect} = this.state;

        if (redirect) {
            const target = this.state.from || "/events";
            return <Navigate to={target} replace/>;
        }

        return (
            <div className="auth-container">
                {isLogin
                    ? this.renderLogin()
                    : step === 2
                        ? this.renderStep2()
                        : this.renderStep1()}
            </div>
        );
    }

}

export default withNavigation(Login);
