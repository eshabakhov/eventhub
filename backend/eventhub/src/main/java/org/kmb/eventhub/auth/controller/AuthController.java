package org.kmb.eventhub.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.auth.dto.AuthRequest;
import org.kmb.eventhub.auth.dto.AuthResponse;
import org.kmb.eventhub.auth.dto.RefreshTokenRequest;
import org.kmb.eventhub.auth.dto.TokensResponse;
import org.kmb.eventhub.auth.service.CustomUserDetailsService;
import org.kmb.eventhub.auth.exception.InvalidCredentialsException;
import org.kmb.eventhub.auth.service.LoginAttemptService;
import org.kmb.eventhub.config.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Авторизация", description = "Аутентификация и авторизация пользователей")
public class AuthController {

    private final JwtUtil jwtUtil;

    private AuthenticationManager authenticationManager;

    private CustomUserDetailsService userDetailsService;

    private LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public void login(@RequestBody AuthRequest request, HttpServletResponse response) {
        String username = request.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            response.setStatus(HttpStatus.LOCKED.value());
            return;
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(username);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        loginAttemptService.resetAttempts(username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Map<String, ResponseCookie> cookieMap = userDetailsService.getAuthCookies(userDetails);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieMap.get("access").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieMap.get("refresh").toString());
    }


    @PostMapping("/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.extractFromCookies(request, "refreshToken");

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        Map<String, ResponseCookie> cookies = jwtUtil.generateTokenCookies(userDetails);

        cookies.values().forEach(cookie ->
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
        );
    }

    @GetMapping("/me")
    public AuthResponse getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {

        if (Objects.isNull(userDetails)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return userDetailsService.createAuthResponse(userDetails);
    }
}
