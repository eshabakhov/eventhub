package org.kmb.eventhub.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.config.FrontendProperties;
import org.kmb.eventhub.config.jwt.JwtUtil;
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

    private final JwtUtil jwtUtil;

    private final FrontendProperties frontendProperties;

    private final UserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = null;
        if (Objects.nonNull(oAuth2User.getAttribute("email"))) {
            email = oAuth2User.getAttribute("email");
        }
        if (Objects.nonNull(oAuth2User.getAttribute("emails"))) {
            List<String> emailList = oAuth2User.getAttribute("emails");
            if (Objects.nonNull(emailList) && !emailList.isEmpty()) {
                email = emailList.getFirst();
            }
        }
        UserDetails userDetails = userDetailsService.loadUserByEmail(email);
        Map<String, ResponseCookie> cookieMap = jwtUtil.generateTokenCookies(userDetails);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieMap.get("access").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieMap.get("refresh").toString());
        response.sendRedirect("http://localhost:3000/");
    }
}
