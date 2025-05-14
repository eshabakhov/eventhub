import api from './AxiosInstance';
import {Component} from "react";

class CurrentUser extends Component {
    static async fetchCurrentUser(setUser) {
        try {
            const response = await api.get('/auth/me');
            const data = response.data;

            setUser({
                id: data.user.id,
                role: data.user.role,
                email: data.user.email,
                username: data.user.username,
                displayName: data.user.displayName,
                loggedIn: true,
                memberLastName: data.customUser.lastName,
                memberFirstName: data.customUser.firstName,
                memberPatronymic: data.customUser.patronymic,
                memberBirthDate: data.customUser.birthDate,
                memberBirthCity: data.customUser.birthCity,
                memberPrivacy: data.customUser.privacy,
                organizerName: data.customUser.name,
                organizerDescription: data.customUser.description,
                organizerIndustry: data.customUser.industry,
                organizerAddress: data.customUser.address,
                organizerAccredited: data.customUser.isAccredited,
                moderatorIsAdmin: data.customUser.isAdmin
            });
        } catch (error) {
            console.error('Ошибка при получении пользователя:', error);
            throw error;
        }
    }
}

export default CurrentUser;
