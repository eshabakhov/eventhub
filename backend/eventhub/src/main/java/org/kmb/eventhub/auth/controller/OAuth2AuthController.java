package org.kmb.eventhub.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/oauth2")
public class OAuth2AuthController {
    @GetMapping("/success")
    public ResponseEntity<String> handleSuccess(@AuthenticationPrincipal OAuth2User user) {
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        // Здесь создать пользователя или найти по email
        return ResponseEntity.ok("Authenticated as: " + name + " (" + email + ")");
    }
}
