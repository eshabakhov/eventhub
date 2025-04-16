import React, { useContext, useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import UserContext from '../contexts/UserContext';
import '../../css/ProfileDropdown.css';

const ProfileDropdown = () => {
  const { user } = useContext(UserContext);
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef(null);

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

  if (!user) return null;

  return (
    <div className="dropdown" ref={dropdownRef}>
      <button className="dropdown-toggle" onClick={toggleDropdown}>
        Профиль
      </button>
      {open && (
        <ul className="dropdown-menu">
          <li><Link to="/profile">{user.displayName || 'Мой профиль'}</Link></li>
          <li><Link to="/friends">Друзья</Link></li>
          <li><Link to="/logout">Выйти</Link></li>
        </ul>
      )}
    </div>
  );
};

export default ProfileDropdown;
