package org.kmb.eventhub.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.exception.InvalidCredentialsException;
import org.kmb.eventhub.service.CustomUserDetailsService;
import org.kmb.eventhub.dto.AuthRequest;
import org.kmb.eventhub.dto.AuthResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?>  login(@RequestBody AuthRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        AuthResponse authResponse = userDetailsService.createAuthResponse(userDetails);

        response.addHeader(HttpHeaders.SET_COOKIE, userDetailsService.getCookieWithJwtToken(userDetails).toString());

        return ResponseEntity.ok(authResponse);
    }
}
