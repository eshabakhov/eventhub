package org.kmb.eventhub.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.config.jwt.JwtAuthenticationEntryPoint;
import org.kmb.eventhub.config.jwt.JwtRequestFilter;
import org.kmb.eventhub.enums.RoleEnum;
import org.kmb.eventhub.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private JwtRequestFilter jwtRequestFilter;

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private CustomUserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                        .logoutUrl("/v1/auth/logout")
                        .addLogoutHandler(jwtCookieLogoutHandler())
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Access Denied\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/v1/users/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/tags").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users").hasRole(RoleEnum.MODERATOR.name())
                        .requestMatchers("/v1/users/{id}").hasRole(RoleEnum.MODERATOR.name())

                        .requestMatchers(HttpMethod.GET, "/v1/users/organizers/{id}").hasAnyRole(RoleEnum.ORGANIZER.name(), RoleEnum.MODERATOR.name())
                        .requestMatchers(HttpMethod.PUT, "/v1/users/organizers/{id}").hasAnyRole(RoleEnum.ORGANIZER.name(), RoleEnum.MODERATOR.name())

                        .requestMatchers(HttpMethod.GET, "/v1/users/organizers/{id}/events").hasRole(RoleEnum.ORGANIZER.name())
                        .requestMatchers(HttpMethod.DELETE, "/v1/users/organizers/{id}/events").hasRole(RoleEnum.ORGANIZER.name())

                        .requestMatchers(HttpMethod.GET, "/v1/users/moderators/{id}").hasRole(RoleEnum.MODERATOR.name())
                        .requestMatchers(HttpMethod.PUT, "/v1/users/moderators/{id}").hasRole(RoleEnum.MODERATOR.name())
                        .requestMatchers(HttpMethod.GET, "/v1/users/members/**").hasRole(RoleEnum.MEMBER.name())
                        .requestMatchers(HttpMethod.PUT, "/v1/users/members/**").hasRole(RoleEnum.MEMBER.name())
                        .requestMatchers(HttpMethod.GET, "/v1/users/members/{id}/events").hasRole(RoleEnum.MEMBER.name())
                        .requestMatchers(HttpMethod.DELETE, "/v1/users/members/{id}/events").hasRole(RoleEnum.MEMBER.name())

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // если используешь куки

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public LogoutHandler jwtCookieLogoutHandler() {
        return (request, response, authentication) -> {
            Cookie cookie = new Cookie("token", null);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            cookie.setSecure(true);
            response.addCookie(cookie);
        };
    }
}
