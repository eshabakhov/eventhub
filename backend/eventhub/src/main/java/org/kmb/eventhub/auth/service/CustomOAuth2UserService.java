package org.kmb.eventhub.auth.service;

import lombok.RequiredArgsConstructor;
import org.kmb.eventhub.user.dto.UserDTO;
import org.kmb.eventhub.user.enums.RoleEnum;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(request);

        String email = "";
        if (Objects.nonNull(user.getAttribute("email"))) {
            email = user.getAttribute("email").toString();
            try {
                userService.getByEmail(email);
            } catch (UserNotFoundException e) {
                UserDTO newUserDTO = new UserDTO();
                newUserDTO.setUsername(email);
                newUserDTO.setEmail(email);
                newUserDTO.setDisplayName(user.getAttribute("name"));
                newUserDTO.setRole(RoleEnum.MEMBER);
                newUserDTO.setPassword(new BCryptPasswordEncoder().encode(generateRandomPassword(12)));
                userService.create(newUserDTO);
            }
        }
        if (Objects.nonNull(user.getAttribute("emails"))) {
            List<String> emailList = user.getAttribute("emails");
            email = emailList.get(0);
            try {
                userService.getByEmail(email);
            } catch (UserNotFoundException e) {
                UserDTO newUserDTO = new UserDTO();
                newUserDTO.setUsername(email);
                newUserDTO.setEmail(email);
                newUserDTO.setDisplayName(user.getAttribute("real_name"));
                newUserDTO.setRole(RoleEnum.MEMBER);
                newUserDTO.setPassword(new BCryptPasswordEncoder().encode(generateRandomPassword(12)));
                userService.create(newUserDTO);
            }
        }
        return user;
    }

    private static String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }
}
