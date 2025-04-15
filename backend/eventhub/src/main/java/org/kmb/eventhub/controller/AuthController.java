package org.kmb.eventhub.controller;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.exception.InvalidCredentialsException;
import org.kmb.eventhub.service.CustomUserDetailsService;
import org.kmb.eventhub.config.jwt.JwtUtil;
import org.kmb.eventhub.dto.AuthRequest;
import org.kmb.eventhub.dto.AuthResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private AuthenticationManager authenticationManager;

    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return userDetailsService.createAuthResponse(userDetailsService.loadUserByUsername(request.getUsername()));
    }
}
