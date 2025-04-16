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
      accreditation: '',

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
    const { isLogin, isRegisterStep2, username, password, confirmPassword, role, email } = this.state;
  
    if (isLogin) {
      // логика входа
      const payload = { username, password };
      fetch('http://localhost:9500/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        credentials: 'include'
      })
        .then(res => {
          if (!res.ok) throw new Error('Ошибка входа');
          return res.json();
        })
        .then(data => {
          const ctx = this.context;
          if (
              //data.token &&
              ctx.setUser) {
            //document.cookie = `token=${data.token}; path=/; Secure; SameSite=Strict`;
            //localStorage.setItem('token', data.token);
            console.log(data); 
            ctx.setUser({ 
              name: username, 
              role: data.role,
              email: data.email,
              loggedIn: true, 
              //token: data.token
            });
            this.setState({ redirect: true }); // ← редирект после входа
          }
        })
        .catch(err => {
          console.error(err);
          alert('Ошибка при входе');
        });
    } else {
      // Регистрация: шаг 1 → шаг 2
      if (!isRegisterStep2) {
        if (!role || !email || !username || !password || password !== confirmPassword) {
          alert('Пожалуйста, заполните все поля корректно');
          return;
        }
        this.setState({ isRegisterStep2: true });
        return;
      }
  
      // Регистрация: шаг 2 — отправка финальных данных
      const payload = {
        username,
        password,
        role,
        email,
        ...this.state.additionalData, // то, что ввели на втором шаге
      };
  
      fetch('http://localhost:9500/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        credentials: 'include'
      })
        .then(res => {
          if (!res.ok) throw new Error('Ошибка регистрации');
          return res.json();
        })
        .then(() => {
          alert('Регистрация завершена');
          this.setState({
            isLogin: true,
            isRegisterStep2: false,
            username: '',
            password: '',
            confirmPassword: '',
            email: '',
            role: '',
            additionalData: {},
          });
        })
        .catch(err => {
          console.error(err);
          alert('Ошибка');
        });
    }
  };
  

  handleLogin = () => {
    const { username, password } = this.state;
    fetch("http://localhost:9500/api/auth/login", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
      credentials: 'include'
    })
      .then(res => res.json())
      .then(data => {
        const ctx = this.context;
        if (
            //.token &&
            ctx.setUser) {
          //localStorage.setItem('token', data.token);
          console.log(data); 
          ctx.setUser({
            name: data.username || username,
            role: data.role,
            email: data.email,
            loggedIn: true,
            //token: data.token
          });
          alert("Успешный вход");
        }
      })
      .catch(err => {
        console.error(err);
        alert("Ошибка входа");
      });
  };

  handleRegistration = () => {
    const {
      username, email, password, role,
      orgName, shortDesc, fullDesc, industry, address, accreditation,
      lastName, firstName, middleName, birthDate, birthCity, privacy
    } = this.state;

    const commonData = { username, email, password, role };

    let roleData = {};
    if (role === 'organizer') {
      roleData = { orgName, shortDesc, fullDesc, industry, address, accreditation };
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
  

  renderStep1() {
    const { username, password, confirmPassword, email, role } = this.state;
  
    return (
      <>
        <h2>Регистрация</h2>
        <form onSubmit={this.handleSubmit}>
          <select name="role" value={role} onChange={this.handleChange} required>
            <option value="">Выберите роль</option>
            <option value="organizer">Организатор</option>
            <option value="participant">Участник</option>
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
          <input
            type="password"
            name="confirmPassword"
            placeholder="Повторите пароль"
            value={confirmPassword}
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
          {role === 'organizer' ? (
            <>
              <input name="orgName" placeholder="Название организации" onChange={this.handleChange} required />
              <input name="shortDesc" placeholder="Краткое описание" onChange={this.handleChange} required />
              <textarea name="fullDesc" placeholder="Полное описание" onChange={this.handleChange} required />
              <input name="industry" placeholder="Сфера деятельности" onChange={this.handleChange} required />
              <input name="address" placeholder="Адрес" onChange={this.handleChange} required />
              <input name="accreditation" placeholder="Аккредитация" onChange={this.handleChange} required />
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
    const { isLogin, isRegisterStep2, redirect } = this.state;

    if (redirect) {
      return <Navigate to="/events" replace />;
    }
  
    return (
      <div className="auth-container">
        {isLogin
          ? this.renderLogin()
          : isRegisterStep2
            ? this.renderStep2()
            : this.renderStep1()}
      </div>
    );
  }
  
}

export default Login;
