package org.kmb.eventhub.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.auth.dto.AuthRequest;
import org.kmb.eventhub.auth.dto.AuthResponse;
import org.kmb.eventhub.auth.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Авторизация", description = "Аутентификация и авторизация пользователей")
public class AuthController {

   private final AuthService authService;

    @PostMapping("/login")
    public void login(@RequestBody AuthRequest request, HttpServletResponse response) {
        authService.login(request, response);
    }


    @PostMapping("/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
       authService.refreshToken(request, response);
    }

    @GetMapping("/me")
    public AuthResponse getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return authService.getCurrentUser(userDetails);
    }
}
