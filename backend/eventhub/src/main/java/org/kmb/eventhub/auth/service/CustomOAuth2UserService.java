package org.kmb.eventhub.auth.service;

import lombok.RequiredArgsConstructor;
import org.kmb.eventhub.tables.daos.UserDao;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserDao userDao;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(request);

        String email = user.getAttribute("email");

        Optional<User> existing = userDao.fetchOptionalByEmail(email);
        if (existing.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            userDao.insert(newUser);
        }

        return user;
    }
}
