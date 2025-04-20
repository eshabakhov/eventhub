import React, { useContext, useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import UserContext from '../../UserContext';
import '../../css/ProfileDropdown.css';

const ProfileDropdown = () => {
  const { user } = useContext(UserContext);
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  const toggleDropdown = () => setOpen((prev) => !prev);

  const handleClickOutside = (event) => {
    if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
      setOpen(false);
    }
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (!user || !user.id) {
    return (
      <div className="profile-dropdown-container" ref={dropdownRef}>
        <button onClick={() => navigate('/login')} className="login-button">
          Войти
        </button>
      </div>
    );
  }

  const { role, displayName, moderatorIsAdmin } = user;

  let menuItems = [];

  if (role === 'MODERATOR') {
    if (moderatorIsAdmin) {
      menuItems = [
        { label: 'Профиль', path: '/profile' },
        { label: 'Управление модераторами', path: '/moderator-management' },
        { label: 'Аккредитация', path: '/accreditation' },
        { label: 'Выйти', path: '/logout' },
      ];
    } else {
      menuItems = [
        { label: 'Профиль', path: '/profile' },
        { label: 'Аккредитация', path: '/accreditation' },
        { label: 'Выйти', path: '/logout' },
      ];
    }
  } else {
    const menuItemsByRole = {
      MEMBER: [
        { label: 'Профиль', path: '/profile' },
        { label: 'Друзья', path: '/friends' },
        { label: 'Мои мероприятия', path: '/my-events' },
        { label: 'Выйти', path: '/logout' },
      ],
      ORGANIZER: [
        { label: 'Профиль', path: '/profile' },
        { label: 'Мои мероприятия', path: '/my-events' },
        { label: 'Выйти', path: '/logout' },
      ],
    };

    menuItems = menuItemsByRole[role] || [
      { label: 'Профиль', path: '/profile' },
      { label: 'Выйти', path: '/logout' },
    ];
  }

  const handleItemClick = (path) => {
    setOpen(false);
    navigate(path);
  };

  return (
    <div className="profile-dropdown-container" ref={dropdownRef}>
      <button className="dropdown-toggle" onClick={toggleDropdown}>
        {displayName || 'Профиль'}
      </button>
      {open && (
        <ul className="dropdown-menu">
          {menuItems.map((item, index) => (
            <li key={index} onClick={() => handleItemClick(item.path)}>
              {item.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default ProfileDropdown;