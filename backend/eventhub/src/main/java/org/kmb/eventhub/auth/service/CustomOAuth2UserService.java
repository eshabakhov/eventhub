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


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(request);

        String email = user.getAttribute("email");

        try {
            userService.getByEmail(email);
        } catch (UserNotFoundException e) {
            UserDTO newUserDTO = new UserDTO();
            newUserDTO.setUsername(email);
            newUserDTO.setEmail(email);
            newUserDTO.setDisplayName(user.getAttribute("name"));
            newUserDTO.setRole(RoleEnum.MEMBER);
            newUserDTO.setPassword(new BCryptPasswordEncoder().encode("Pa55w0rd"));
            userService.create(newUserDTO);
        }

        return user;
    }
}
