// ProfilePage.jsx
import React, { Component } from 'react';
import UserContext from '../../UserContext';
import { useNavigate } from 'react-router-dom';
import "../../css/ProfilePage.css";
import EventHubLogo from "../../img/eventhub.png";
import ProfileDropdown from "../profile/ProfileDropdown";

function ProfilePageWithNavigation(props) {
  const navigate = useNavigate();
  return <ProfilePage {...props} navigate={navigate} />;
}

class ProfilePage extends Component {
  static contextType = UserContext;

  state = {
    formData: {},
    loading: true,
    successMessage: ''
  };

  componentDidMount() {
    const { user } = this.context;

    if (user && user.id) {
      fetch('http://localhost:9500/api/auth/me', {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      })
        .then(async (response) => {
          if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
          }
          const data = await response.json();

          const formData = {
            role: data.user.role || '',
            email: data.user.email || '',
            username: data.user.username || '',
            displayName: data.user.displayName || '',
            password: '',
            ...(data.customUser && {
              organizationName: data.customUser.name || '',
              description: data.customUser.description || '',
              industry: data.customUser.industry || '',
              address: data.customUser.address || '',
              accreditation: data.customUser.isAccredited || '',
              lastName: data.customUser.lastName || '',
              firstName: data.customUser.firstName || '',
              patronymic: data.customUser.patronymic || '',
              birthDate: data.customUser.birthDate || '',
              birthCity: data.customUser.birthCity || '',
              privacy: data.customUser.privacy || 'public',
              isAdmin: !!data.customUser.isAdmin
            })
          };

          this.setState({ formData, loading: false });
        })
        .catch((error) => {
          console.error('Ошибка при загрузке профиля:', error);
          this.setState({ loading: false });
        });
    }
  }

  handleChange = (e) => {
    const { name, type, checked, value } = e.target;
    this.setState((prevState) => ({
      formData: {
        ...prevState.formData,
        [name]: type === 'checkbox' ? checked : value,
      }
    }));
  };

  handleSubmit = async (e) => {
    e.preventDefault();
    const { user, setUser } = this.context;
    const { formData } = this.state;
  
    const rolePathMap = {
      ORGANIZER: 'organizers',
      MEMBER: 'members',
      MODERATOR: 'moderators'
    };
  
    const rolePath = rolePathMap[user.role];
    const commonEndpoint = `http://localhost:9500/api/v1/users/${user.id}`;
    const roleEndpoint = rolePath
      ? `http://localhost:9500/api/v1/users/${rolePath}/${user.id}`
      : null;
  
    const {
      role,
      email,
      username,
      displayName,
      password,
      ...restFields
    } = formData;
  
    const commonFields = {
      role,
      email,
      username,
      displayName,
      password
    };
  
    try {
      // 1. Обновление общих данных
      const commonResponse = await fetch(commonEndpoint, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(commonFields)
      });
  
      if (!commonResponse.ok) {
        throw new Error(`Ошибка при обновлении общих данных: ${commonResponse.status}`);
      }
  
      // 2. Обновление ролевых данных
      if (roleEndpoint) {
        const roleResponse = await fetch(roleEndpoint, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(restFields)
        });
  
        if (!roleResponse.ok) {
          throw new Error(`Ошибка при обновлении ролевых данных: ${roleResponse.status}`);
        }
      }
  
      // 3. Загрузка обновлённых данных
      const meResponse = await fetch('http://localhost:9500/api/auth/me', {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });
  
      if (!meResponse.ok) {
        throw new Error(`Ошибка при получении данных пользователя: ${meResponse.status}`);
      }
  
      const data = await meResponse.json();
  
      const updatedFormData = {
        role: data.user.role || '',
        email: data.user.email || '',
        username: data.user.username || '',
        displayName: data.user.displayName || '',
        password: '',
        ...(data.customUser && {
          organizationName: data.customUser.name || '',
          description: data.customUser.description || '',
          industry: data.customUser.industry || '',
          address: data.customUser.address || '',
          accreditation: data.customUser.isAccredited || '',
          lastName: data.customUser.lastName || '',
          firstName: data.customUser.firstName || '',
          patronymic: data.customUser.patronymic || '',
          birthDate: data.customUser.birthDate || '',
          birthCity: data.customUser.birthCity || '',
          privacy: data.customUser.privacy || 'public',
          isAdmin: !!data.customUser.isAdmin
        })
      };
  
      // 4. Обновление состояния и контекста
      this.setState({
        formData: updatedFormData,
        successMessage: 'Профиль успешно обновлён'
      });
  
      setUser(data.user);
  
      setTimeout(() => this.setState({ successMessage: '' }), 3000);
    } catch (error) {
      console.error('Ошибка при обновлении профиля:', error);
    }
  };  

  getRoleDisplayName = (role) => {
    switch (role) {
      case 'MEMBER':
        return 'Участник';
      case 'ORGANIZER':
        return 'Организатор';
      case 'MODERATOR':
        return 'Модератор';
      default:
        return 'Неизвестно';
    }
  };

  handleBack = () => {
    this.props.navigate('/events');
  };

  render() {
    const { navigate } = this.props;
    const { user } = this.context;
    const { formData, loading, successMessage } = this.state;

    if (loading) return <div className="profile-loading">Загрузка...</div>;

    return (
      <div>
        <div className="header-bar">
                <div className="top-logo" onClick={() => navigate("/events")} style={{ cursor: "pointer" }}>
                    <img src={EventHubLogo} alt="Logo" className="logo" />
                </div>
                <h1 className="friends-title">Мои друзья</h1>
                <div className="login-button-container">
                    <ProfileDropdown navigate={navigate} />
                </div>
        </div>
        <div className="profile-container">
          <div className="back-area" onClick={this.handleBack}>
            <button className="back-button">←</button>
          </div>
          <div className="profile-card">
            <h2 className="profile-title">Профиль пользователя</h2>
            <form onSubmit={this.handleSubmit}>
              {user.role === 'ORGANIZER' && (
                <div className="profile-label">
                  {formData.accreditation ? (
                    <span className="accreditation-status accredited">Организация аккредитована</span>
                  ) : (
                    <span className="accreditation-status not-accredited">Организация не аккредитована</span>
                  )}
                </div>
              )}

              {/* Общие поля */}
              <label className="profile-label">
                Роль:
                <input className="profile-input" type="text" name="role" value={this.getRoleDisplayName(formData.role)} disabled />
              </label>
              <label className="profile-label">
                Почта:
                <input className="profile-input" type="email" name="email" value={formData.email || ''} onChange={this.handleChange} disabled />
              </label>
              <label className="profile-label">
                Логин:
                <input className="profile-input" type="text" name="username" value={formData.username} onChange={this.handleChange} />
              </label>
              <label className="profile-label">
                Отображаемое имя:
                <input className="profile-input" type="text" name="displayName" value={formData.displayName} onChange={this.handleChange} />
              </label>
              <label className="profile-label">
                Пароль:
                <input className="profile-input" type="password" name="password" value={formData.password} onChange={this.handleChange} />
              </label>

              {/* Организатор */}
              {user.role === 'ORGANIZER' && (
                <>
                  <label className="profile-label">
                    Название:
                    <input className="profile-input" type="text" name="organizationName" value={formData.organizationName} onChange={this.handleChange} />
                  </label>
                  <label className="profile-label">
                    Описание:
                    <textarea className="profile-input" name="description" value={formData.description} onChange={this.handleChange} />
                  </label>
                  <label className="profile-label">
                    Сфера деятельности:
                    <input className="profile-input" type="text" name="industry" value={formData.industry} onChange={this.handleChange} />
                  </label>
                  <label className="profile-label">
                    Адрес:
                    <input className="profile-input" type="text" name="address" value={formData.address} onChange={this.handleChange} />
                  </label>
                </>
              )}

              {/* Участник */}
              {user.role === 'MEMBER' && (
                <>
                  <label className="profile-label">
                    Фамилия:
                    <input className="profile-input" type="text" name="lastName" value={formData.lastName} onChange={this.handleChange} />
                  </label>
                  <label className="profile-label">
                    Имя:
                    <input className="profile-input" type="text" name="firstName" value={formData.firstName} onChange={this.handleChange} />
                  </label>
                  <label className="profile-label">
                    Отчество:
                    <input className="profile-input" type="text" name="patronymic" value={formData.patronymic} onChange={this.handleChange} />
                  </label>
                  <label className="profile-label">
                    Дата рождения:
                    <input className="profile-input" type="date" name="birthDate" value={formData.birthDate} onChange={this.handleChange} />
                  </label>
                  <label className="profile-label">
                    Город рождения:
                    <input className="profile-input" type="text" name="birthCity" value={formData.birthCity} onChange={this.handleChange} />
                  </label>
                  <label className="profile-label">
                    Приватность:
                    <select className="profile-input" name="privacy" value={formData.privacy} onChange={this.handleChange}>
                      <option value="public">Публично</option>
                      <option value="private">Приватно</option>
                    </select>
                  </label>
                </>
              )}

              <div className="card-buttons mt-4">
                <button type="submit" className="profile-button">Сохранить изменения</button>
              </div>
              {successMessage && <p className="success-message">{successMessage}</p>}
            </form>
          </div>
        </div>
      </div>
    );
  }
}

export default ProfilePageWithNavigation;