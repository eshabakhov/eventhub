import axios from 'axios';
import API_BASE_URL from "../../config"

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // отправка http-only куки
});

api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        await api.post('/auth/refresh'); // обновление токена, куки отправятся автоматически
        return api(originalRequest);     // повторный запрос
      } catch (refreshError) {
        window.location.href = '/login'; // редирект на логин при неудаче
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);

  }
);

export default api;
