import React, { Component } from 'react';
import UserContext from '../../UserContext';
import axios from 'axios';
import "../../css/ProfilePage.css";

class ProfilePage extends Component {
  static contextType = UserContext;

  state = {
    formData: {},
    loading: true,
    successMessage: ''
  };

  componentDidMount() {
    const { user } = this.context;
    console.log(user)
    if (user && user.id) {
      axios.get(`/api/users/${user.id}`)
        .then((response) => {
          this.setState({ formData: response.data, loading: false });
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
  
    console.log(this.state.formData)
    try {
      const response = await fetch(`http://localhost:9500/api/v1/users/${user.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'  
          // Добавь Authorization, если нужен JWT:
          // 'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(this.state.formData),
        credentials: 'include' // если нужно отправлять куки
      });
  
  
      if (!response.ok) {
        throw new Error(`Ошибка HTTP: ${response.status}`);
      }
  
      const updatedUser = await response.json();
      setUser(updatedUser);
      this.setState({ successMessage: 'Профиль успешно обновлён' });
  
      setTimeout(() => this.setState({ successMessage: '' }), 3000);
    } catch (error) {
      console.error('Ошибка при обновлении профиля:', error);
    }
  };
  

  render() {
    const { user } = this.context;
    const { formData, loading, successMessage } = this.state;

    if (loading) return <div className="profile-loading">Загрузка...</div>;

    return (
      <div className="profile-container">
        <div className="profile-card">
          <h2 className="profile-title">Профиль пользователя</h2>
          <form onSubmit={this.handleSubmit}>
            {/* Общие поля */}
            <label className="profile-label">
              Роль:
              <input
                className="profile-input"
                type="text"
                name="role"
                value={formData.role || user.role}
                readOnly
              />
            </label>
            <label className="profile-label">
              Почта:
              <input
                className="profile-input"
                type="email"
                name="email"
                value={formData.email || user.email}
                onChange={this.handleChange}
              />
            </label>
            <label className="profile-label">
              Логин:
              <input
                className="profile-input"
                type="text"
                name="username"
                value={formData.username || user.username}
                onChange={this.handleChange}
              />
            </label>
            <label className="profile-label">
              Отображаемое имя:
              <input
                className="profile-input"
                type="text"
                name="displayName"
                value={formData.displayName || user.displayName}
                onChange={this.handleChange}
              />
            </label>
            <label className="profile-label">
              Пароль:
              <input
                className="profile-input"
                type="password"
                name="password"
                value={formData.password || user.password}
                onChange={this.handleChange}
              />
            </label>

            {/* Поля Организатора */}
            {user.role === 'ORGANIZER' && (
              <>
                <label className="profile-label">
                  Название:
                  <input
                    className="profile-input"
                    type="text"
                    name="organizationName"
                    value={formData.organizationName || user.organizerName}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Описание:
                  <textarea
                    className="profile-input"
                    name="description"
                    value={formData.description || user.organizerDescription}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Сфера деятельности:
                  <input
                    className="profile-input"
                    type="text"
                    name="industry"
                    value={formData.industry || user.organizerIndustry}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Адрес:
                  <input
                    className="profile-input"
                    type="text"
                    name="address"
                    value={formData.address || user.organizerAddress}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Аккредитация:
                  <input
                    className="profile-input"
                    type="text"
                    name="accreditation"
                    value={formData.accreditation || user.organizerAccredited}
                    onChange={this.handleChange}
                  />
                </label>
              </>
            )}

            {/* Поля Участника */}
            {user.role === 'MEMBER' && (
              <>
                <label className="profile-label">
                  Фамилия:
                  <input
                    className="profile-input"
                    type="text"
                    name="lastName"
                    value={formData.lastName || user.memberLastName}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Имя:
                  <input
                    className="profile-input"
                    type="text"
                    name="firstName"
                    value={formData.firstName || user.memberFirstName}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Отчество:
                  <input
                    className="profile-input"
                    type="text"
                    name="middleName"
                    value={formData.middleName || user.memberPatronymic}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Дата рождения:
                  <input
                    className="profile-input"
                    type="date"
                    name="birthDate"
                    value={formData.birthDate || user.memberBirthDate}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Город рождения:
                  <input
                    className="profile-input"
                    type="text"
                    name="birthCity"
                    value={formData.birthCity || user.memberBirthCity}
                    onChange={this.handleChange}
                  />
                </label>
                <label className="profile-label">
                  Приватность:
                  <select
                    className="profile-input"
                    name="privacy"
                    value={formData.privacy || user.mebmerPrivacy}
                    onChange={this.handleChange}
                  >
                    <option value="public">Публично</option>
                    <option value="private">Приватно</option>
                  </select>
                </label>
              </>
            )}

            {/* Поля Модератора */}
            {user.role === 'MODERATOR' && (
              <label className="profile-label">
                Администратор:
                <input
                  className="profile-input"
                    type="checkbox"
                    name="isAdmin"
                    checked={formData.isAdmin || user.moderatorIsAdmin}
                    onChange={this.handleChange}
                  />
              </label>
            )}

            <div className="card-buttons mt-4">
              <button type="submit" className="profile-button">
                Сохранить изменения 
              </button>
            </div>
            {successMessage && <p className="success-message">{successMessage}</p>}
          </form>
        </div>
      </div>
    );
  }
}

export default ProfilePage;