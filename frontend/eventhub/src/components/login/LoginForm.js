import React from "react"
import "../../css/AuthPage.css";
import UserContext from "../../UserContext";
import { Navigate } from "react-router";
import { em } from "framer-motion/client";

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
      privacy: 'private',

      redirect: false
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
    this.setState({ [e.target.name]: e.target.value });
  };

  handleNext = e => {
    e.preventDefault();
    const { username, email, password, confirmPassword, role } = this.state;
    if (!username || !email || !password || password !== confirmPassword || !role) {
      alert("Пожалуйста, заполните все поля корректно");
      return;
    }
    this.setState({ step: 2 });
  };

  handleSubmit = (e) => {
    e.preventDefault();
    const {
      step, username, password, confirmPassword, role, email, displayName,
      orgName, shortDesc, fullDesc, industry, address,
      lastName, firstName, middleName, birthDate, birthCity, privacy
    } = this.state;
  
    if (step !== 2) return;
  
    let roleValid = true;
  
    if (role === "ORGANIZER") {
      roleValid = orgName && shortDesc && fullDesc && industry && address;
    } else if (role === "MEMBER") {
      roleValid = lastName && firstName && middleName && birthDate && birthCity && privacy;
    }
  
    if (!roleValid) {
      alert("Пожалуйста, заполните все поля на втором шаге");
      return;
    }
  
    const commonData = {
      username,
      email,
      password,
      displayName,
      role
    };
  
    let createdUserId = null;
  
    // 1. Создаём пользователя
    fetch("http://localhost:9500/api/v1/users", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(commonData)
    })
      .then(res => {
        if (!res.ok) throw new Error("Ошибка при создании пользователя");
        return res.json();
      })
      .then(user => {
        createdUserId = user.id;
  
        // 2. Авторизуем нового пользователя
        return fetch("http://localhost:9500/api/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify({ username, password })
        });
      })
      .then(res => {
        if (!res.ok) throw new Error("Ошибка при авторизации нового пользователя");
  
        // 3. Отправляем данные по роли
        let rolePayload = {};
        let url = "";
  
        if (role === "ORGANIZER") {
          url = `http://localhost:9500/api/v1/users/organizers/${createdUserId}`;
          rolePayload = {
            name: orgName,
            description: fullDesc,
            shortDescription: shortDesc,
            industry,
            address
          };
        } else if (role === "MEMBER") {
          url = `http://localhost:9500/api/v1/users/members/${createdUserId}`;
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
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify(rolePayload)
        });
      })
      .then(res => {
        if (!res.ok) throw new Error("Ошибка при обновлении данных роли");
        this.setState({
          redirect: true
        });
      })      
      .catch(err => {
        console.error(err);
        alert("Ошибка при регистрации: " + err.message);
      });
  };    
  
  handleRegistration = () => {
    const {
      username, email, password, role,
      orgName, shortDesc, fullDesc, industry, address,
      lastName, firstName, middleName, birthDate, birthCity, privacy
    } = this.state;

    const commonData = { username, email, password, role };

    let roleData = {};
    if (role === 'organizer') {
      roleData = { orgName, shortDesc, fullDesc, industry, address };
    } else {
      roleData = { lastName, firstName, middleName, birthDate, birthCity, privacy };
    }

    const payload = { ...commonData, ...roleData };

    fetch("http://localhost:9500/api/auth/register", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      credentials: 'include'
    })
      .then(res => res.json())
      .then(data => {
        alert("Регистрация успешна!");
        this.setState({ isLogin: true, step: 1 }); // Возврат к логину
      })
      .catch(err => {
        console.error(err);
        alert("Ошибка регистрации");
      });
  };

  renderLogin() {
    const { username, password } = this.state;
  
    return (
      <>
        <h2>Вход</h2>
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
          <button type="submit">Войти</button>
        </form>
        <p onClick={this.handleToggle} className="toggle-link">
          Нет аккаунта? Зарегистрироваться
        </p>
      </>
    );
  }
  
  handleNext = (e) => {
    e.preventDefault();
    const { username, email, password, confirmPassword, role, displayName } = this.state;
  
    if (!username || !email || !password || password !== confirmPassword || !role || !displayName) {
      alert("Пожалуйста, заполните все поля корректно");
      return;
    }
  
    this.setState({ step: 2 });
  };  

  renderStep1() {
    const { username, password, confirmPassword, email, role, displayName } = this.state;
  
    return (
      <>
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
          <button type="submit">Далее</button>
        </form>
        <p onClick={this.handleToggle} className="toggle-link">
          Уже есть аккаунт? Войти
        </p>
      </>
    );
  }
  

  renderStep2() {
    const { role } = this.state;
    return (
      <>
        <h2>Регистрация</h2>
        <form onSubmit={this.handleSubmit}>
          {role === 'ORGANIZER' ? (
            <>
              <input name="orgName" placeholder="Название организации" onChange={this.handleChange} required />
              <input name="shortDesc" placeholder="Краткое описание" onChange={this.handleChange} required />
              <textarea name="fullDesc" placeholder="Полное описание" onChange={this.handleChange} required />
              <input name="industry" placeholder="Сфера деятельности" onChange={this.handleChange} required />
              <input name="address" placeholder="Адрес" onChange={this.handleChange} required />
            </>
          ) : (
            <>
              <input name="lastName" placeholder="Фамилия" onChange={this.handleChange} required />
              <input name="firstName" placeholder="Имя" onChange={this.handleChange} required />
              <input name="middleName" placeholder="Отчество" onChange={this.handleChange} required />
              <input type="date" name="birthDate" onChange={this.handleChange} required />
              <input name="birthCity" placeholder="Город рождения" onChange={this.handleChange} required />
              <select name="privacy" onChange={this.handleChange}>
                <option value="private">Приватный</option>
                <option value="public">Публичный</option>
                <option value="friends">Только для друзей</option>
              </select>
            </>
          )}
          <button type="submit">Зарегистрироваться</button>
        </form>
      </>
    );
  }
  

  render() {
    const { isLogin, step, redirect } = this.state;

    if (redirect) {
      return <Navigate to="/events" replace />;
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

export default Login;
