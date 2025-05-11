package org.kmb.eventhub.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.auth.dto.AuthRequest;
import org.kmb.eventhub.auth.dto.AuthResponse;
import org.kmb.eventhub.auth.service.CustomUserDetailsService;
import org.kmb.eventhub.auth.exception.InvalidCredentialsException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Авторизация", description = "Аутентфикация и авторизация пользователей")
public class AuthController {

    private AuthenticationManager authenticationManager;

    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public void login(@RequestBody AuthRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        response.addHeader(HttpHeaders.SET_COOKIE, userDetailsService.getCookieWithJwtToken(userDetails).toString());
    }

    @GetMapping("/me")
    public AuthResponse getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {

        if (Objects.isNull(userDetails)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return userDetailsService.createAuthResponse(userDetails);
    }
}
