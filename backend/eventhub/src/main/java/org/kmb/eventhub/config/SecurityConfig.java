package org.kmb.eventhub.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.config.jwt.JwtAuthenticationEntryPoint;
import org.kmb.eventhub.config.jwt.JwtRequestFilter;
import org.kmb.eventhub.user.enums.RoleEnum;
import org.kmb.eventhub.auth.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String V1_USERS = "/v1/users";
    private static final String V1_USERS_ID = "/v1/users/{id}";

    private static final String V1_EVENTS = "/v1/events";
    private static final String V1_EVENTS_ID = "/v1/events/{id}";
    private static final String V1_TAGS = "/v1/tags";

    private static final String V1_USERS_ORGANIZERS_ID = "/v1/users/organizers/{id}";

    private static final String V1_USERS_ORGANIZERS_ID_EVENTS = "/v1/users/organizers/{id}/events";

    private static final String V1_USERS_MODERATORS_ID = "/v1/users/moderators/{id}";

    private static final String V1_USERS_MEMBERS_ALL = "/v1/users/members/**";

    private static final String V1_MEMBERS_ID_EVENTS = "/v1/members/{memberId}/events";

    private static final String V1_MEMBERS_ID_SUBSCRIBE = "/v1/members/{memberId}/subscribe/{eventId}";

    private JwtRequestFilter jwtRequestFilter;

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private CustomUserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

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
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_OK)))
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
                        .requestMatchers(
                                "/login", "/login/oauth2/**", "/oauth2/**",
                                "/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                V1_EVENTS_ID
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, V1_USERS).permitAll()
                        .requestMatchers(HttpMethod.POST, V1_USERS).permitAll()
                        .requestMatchers(HttpMethod.GET, V1_USERS).hasRole(RoleEnum.MODERATOR.name())

                        .requestMatchers(V1_USERS_ID).hasRole(RoleEnum.MODERATOR.name())
                        .requestMatchers(HttpMethod.GET, V1_USERS_ID).permitAll()
                        .requestMatchers(HttpMethod.PUT, V1_USERS_ID).permitAll()

                        .requestMatchers(HttpMethod.GET, V1_USERS_ORGANIZERS_ID).hasAnyRole(RoleEnum.ORGANIZER.name(), RoleEnum.MODERATOR.name())
                        .requestMatchers(HttpMethod.PUT, V1_USERS_ORGANIZERS_ID).hasAnyRole(RoleEnum.ORGANIZER.name(), RoleEnum.MODERATOR.name())
                        .requestMatchers(HttpMethod.GET, V1_USERS_MODERATORS_ID).hasRole(RoleEnum.MODERATOR.name())
                        .requestMatchers(HttpMethod.PUT, V1_USERS_MODERATORS_ID).hasRole(RoleEnum.MODERATOR.name())
                        .requestMatchers(HttpMethod.GET, V1_USERS_MEMBERS_ALL).hasRole(RoleEnum.MEMBER.name())
                        .requestMatchers(HttpMethod.PUT, V1_USERS_MEMBERS_ALL).hasRole(RoleEnum.MEMBER.name())

                        .requestMatchers(HttpMethod.GET, V1_USERS_ORGANIZERS_ID_EVENTS).hasRole(RoleEnum.ORGANIZER.name())
                        .requestMatchers(HttpMethod.DELETE, V1_USERS_ORGANIZERS_ID_EVENTS).hasRole(RoleEnum.ORGANIZER.name())
                        .requestMatchers(HttpMethod.GET, V1_MEMBERS_ID_SUBSCRIBE).hasRole(RoleEnum.MEMBER.name())
                        .requestMatchers(HttpMethod.DELETE, V1_MEMBERS_ID_SUBSCRIBE).hasRole(RoleEnum.MEMBER.name())
                        .requestMatchers(HttpMethod.POST, V1_MEMBERS_ID_SUBSCRIBE).hasRole(RoleEnum.MEMBER.name())
                        .requestMatchers(HttpMethod.GET, V1_MEMBERS_ID_EVENTS).hasRole(RoleEnum.MEMBER.name())

                        .requestMatchers(HttpMethod.GET, V1_EVENTS).permitAll()
                        .requestMatchers(HttpMethod.GET, V1_TAGS).permitAll()
                        .requestMatchers(HttpMethod.GET, V1_EVENTS_ID).permitAll()

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login/oauth2/google") // Указывает, что путь /login будет использовать OAuth2 с Google
                        .defaultSuccessUrl("/auth/oauth2/success", true) // Успешный редирект после авторизации
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
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
