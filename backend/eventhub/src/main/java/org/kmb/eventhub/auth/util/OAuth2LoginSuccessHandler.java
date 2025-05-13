package org.kmb.eventhub.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.kmb.eventhub.auth.service.CustomUserDetailsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        if (Objects.nonNull(oAuth2User.getAttribute("email"))) {
            String email = oAuth2User.getAttribute("email");
            UserDetails userDetails = customUserDetailsService.loadUserByEmail(email);
            Map<String, ResponseCookie> cookieMap = customUserDetailsService.getAuthCookies(userDetails);
            response.addHeader(HttpHeaders.SET_COOKIE, cookieMap.get("access").toString());
            response.addHeader(HttpHeaders.SET_COOKIE, cookieMap.get("refresh").toString());
            response.sendRedirect("http://localhost:3000/");
        }
        if (Objects.nonNull(oAuth2User.getAttribute("emails"))) {
            List<String> emailList = oAuth2User.getAttribute("emails");
            String email = emailList.get(0);
            UserDetails userDetails = customUserDetailsService.loadUserByEmail(email);
            Map<String, ResponseCookie> cookieMap = customUserDetailsService.getAuthCookies(userDetails);
            response.addHeader(HttpHeaders.SET_COOKIE, cookieMap.get("access").toString());
            response.addHeader(HttpHeaders.SET_COOKIE, cookieMap.get("refresh").toString());
            response.sendRedirect("http://localhost:3000/");
        }
    }
}
