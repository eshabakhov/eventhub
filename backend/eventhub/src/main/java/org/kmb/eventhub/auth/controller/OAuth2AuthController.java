package org.kmb.eventhub.auth.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.tables.pojos.User;
import org.kmb.eventhub.user.dto.UserDTO;
import org.kmb.eventhub.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/auth/oauth2")
public class OAuth2AuthController {

    private final UserService userService;

    @GetMapping("/success")
    public ResponseEntity<String> handleSuccess(@AuthenticationPrincipal OAuth2User user) {
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        // Здесь создать пользователя или найти по email
        return ResponseEntity.ok("Authenticated as: " + name + " (" + email + ")");
    }

    @PostMapping("/setup")
    public User setupUser(@RequestBody @Valid UserDTO userDTO,
                          @CookieValue("token") String token) {
        return userService.create(userDTO);
    }
}
