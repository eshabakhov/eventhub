import React from "react"
import "../../css/AuthPage.css";

class Login extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
          isLogin: true,
          username: '',
          password: '',
          confirmPassword: '',
        };
      }
    
      handleToggle = () => {
        this.setState(prevState => ({
          isLogin: !prevState.isLogin,
          username: '',
          password: '',
          confirmPassword: ''
        }));
      };
    
      handleChange = e => {
        this.setState({ [e.target.name]: e.target.value });
      };
    
      handleSubmit = e => {
        e.preventDefault();
        const { isLogin, username, password, confirmPassword } = this.state;
    
        if (!username || !password || (!isLogin && password !== confirmPassword)) {
          alert('Проверьте корректность данных');
          return;
        }
    
        const payload = { username, password };
    
        const endpoint = isLogin
            ? 'http://localhost:9500/api/auth/login'
            : 'http://localhost:9500/api/auth/register';
    
        fetch(endpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
          credentials: 'include'
        })
          .then(res => {
            if (!res.ok) throw new Error('Ошибка при отправке запроса');
            return res.json();
          })
          .then(data => {
            console.log('Успешно:', data);
            if (isLogin && data.token) {
              localStorage.setItem('token', data.token);
              alert('Успешный вход');
            } else {
              alert('Успешная регистрация');
            }
          })
          .catch(err => {
            console.error(err);
            alert('Ошибка');
          });
      };
    
      render() {
        const { isLogin, username, password, confirmPassword } = this.state;
    
        return (
          <div className="auth-container">
            <h2>{isLogin ? 'Вход' : 'Регистрация'}</h2>
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
              {!isLogin && (
                <input
                  type="password"
                  name="confirmPassword"
                  placeholder="Повторите пароль"
                  value={confirmPassword}
                  onChange={this.handleChange}
                  required
                />
              )}
              <button type="submit">{isLogin ? 'Войти' : 'Зарегистрироваться'}</button>
            </form>
            <p onClick={this.handleToggle} className="toggle-link">
              {isLogin ? 'Нет аккаунта? Зарегистрироваться' : 'Уже есть аккаунт? Войти'}
            </p>
          </div>
        );
      }
}

export default Login