import React, { useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
import UserContext from "../../UserContext";
import API_BASE_URL from "../../config";

const LogoutPage = () => {
    const { setUser } = useContext(UserContext);
    const navigate = useNavigate();

    useEffect(() => {
        // Отправим запрос на logout (если сервер его поддерживает)
        fetch(`${API_BASE_URL}/auth/logout`, {
            method: "POST",
            credentials: "include",
        })
        .catch(err => {
            console.error("Ошибка при выходе:", err);
        })
        .finally(() => {
            // Очищаем пользователя из контекста
            setUser(null);
            // Перенаправляем на главную или логин
            navigate("/login", { replace: true });
        });
    }, [setUser, navigate]);

    return <p>Выход...</p>;
};

export default LogoutPage;
